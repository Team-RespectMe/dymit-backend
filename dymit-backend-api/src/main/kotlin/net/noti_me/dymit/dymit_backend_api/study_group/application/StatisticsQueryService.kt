package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetGroupMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetGroupStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.RefreshMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

/** 개인 및 그룹 통계 조회를 공통 구성원 갱신 경로로 처리합니다. */
@Service
class StatisticsQueryService(
    private val memberRepository: StudyGroupMemberRepository,
    private val statisticsRepository: StatisticsRepository,
    private val refreshMemberStatistics: RefreshMemberStatisticsUseCase
) : GetMemberStatisticsUseCase, GetGroupStatisticsUseCase, GetGroupMemberStatisticsUseCase {

    override fun execute(command: GetMemberStatisticsCommand): MemberStatisticsDto {
        val groupId = ObjectId(command.groupId)
        val requester = requireActiveMember(groupId, ObjectId(command.requesterId))
        val target = requireActiveMember(groupId, ObjectId(command.memberId))
        if (requester.memberId != target.memberId && !requester.isManager()) {
            throw ForbiddenException(message = "본인 또는 그룹 관리자만 구성원 통계를 조회할 수 있습니다.")
        }
        val observedAt = Instant.now()
        return refresh(target, StatisticsWeekWindow.cutoffAt(observedAt), observedAt)
    }

    override fun execute(command: GetGroupStatisticsCommand): GroupStatisticsDto {
        val groupId = ObjectId(command.groupId)
        requireManager(groupId, ObjectId(command.requesterId))
        val observedAt = Instant.now()
        val cutoff = StatisticsWeekWindow.cutoffAt(observedAt)
        val memberships = refreshAllMemberships(groupId, cutoff, observedAt)
        val firstWeek = memberships.mapNotNull { it.createdAt }
            .minOrNull()
            ?.let(StatisticsWeekWindow::firstWeekEndAfter)
            ?: cutoff
        val latestStored = statisticsRepository.findLatestGroupWeek(groupId, cutoff)
        var weekEnd = latestStored?.weekEnd?.plusSeconds(WEEK_SECONDS) ?: firstWeek
        while (weekEnd <= cutoff) {
            saveGroupWeek(groupId, weekEnd, observedAt)
            weekEnd = weekEnd.plusSeconds(WEEK_SECONDS)
        }
        val current = saveGroupWeek(groupId, cutoff, observedAt)
        val previousWeekEnd = cutoff.minusSeconds(WEEK_SECONDS)
        val previous = saveGroupWeek(groupId, previousWeekEnd, observedAt)
        val currentTaskRate = current.counts.taskSubmissionRate()
        val currentScheduleRate = current.counts.scheduleAttendanceRate()
        val previousTaskRate = previous.counts.taskSubmissionRate()
        val previousScheduleRate = previous.counts.scheduleAttendanceRate()
        return GroupStatisticsDto(
            groupId = command.groupId,
            weekEnd = cutoff,
            counts = current.counts,
            taskSubmissionRate = currentTaskRate,
            scheduleAttendanceRate = currentScheduleRate,
            previousTaskSubmissionRate = previousTaskRate,
            previousScheduleAttendanceRate = previousScheduleRate
        )
    }

    override fun execute(command: GetGroupMemberStatisticsCommand): List<MemberStatisticsDto> {
        if (command.size !in 1..MAX_PAGE_SIZE) {
            throw BadRequestException(message = "size는 1 이상 $MAX_PAGE_SIZE 이하여야 합니다.")
        }
        val groupId = ObjectId(command.groupId)
        requireManager(groupId, ObjectId(command.requesterId))
        val memberships = memberRepository.findActiveByGroupId(
            groupId = groupId,
            cursor = command.cursor?.let(::ObjectId),
            limit = command.size + 1
        )
        val observedAt = Instant.now()
        val cutoff = StatisticsWeekWindow.cutoffAt(observedAt)
        return memberships.map { refresh(it, cutoff, observedAt) }
    }

    private fun refreshAllMemberships(
        groupId: ObjectId,
        cutoff: Instant,
        observedAt: Instant
    ): List<StudyGroupMember> {
        val result = mutableListOf<StudyGroupMember>()
        var cursor: ObjectId? = null
        do {
            val page = memberRepository.findByGroupIdIncludingDeleted(groupId, cursor, INTERNAL_PAGE_SIZE)
            page.filter { it.createdAt?.let { joinedAt -> joinedAt < cutoff } == true }
                .filter { membership ->
                    val terminalCutoff = membership.deletedAt
                        ?.let(StatisticsWeekWindow::firstWeekEndAfter)
                    val ledger = membership.id?.let(statisticsRepository::findLedger)
                    terminalCutoff == null || ledger == null ||
                        ledger.taskCalculatedThrough < terminalCutoff ||
                        ledger.scheduleCalculatedThrough < terminalCutoff ||
                        !hasCompleteFrozenProjection(membership, ledger, terminalCutoff)
                }
                .forEach { refresh(it, cutoff, observedAt) }
            result.addAll(page)
            cursor = page.lastOrNull()?.id
        } while (page.size == INTERNAL_PAGE_SIZE)
        return result
    }

    private fun hasCompleteFrozenProjection(
        membership: StudyGroupMember,
        ledger: MemberStatisticsLedger,
        terminalCutoff: Instant
    ): Boolean {
        val membershipId = membership.id ?: return false
        val joinedAt = membership.createdAt ?: return false
        val snapshots = statisticsRepository.findMemberWeeks(membershipId, terminalCutoff)
        return StatisticsWeekWindow.weekEnds(joinedAt, terminalCutoff).all { weekEnd ->
            snapshots.any { snapshot ->
                snapshot.weekEnd == weekEnd &&
                    (snapshot.weekEnd < ledger.projectionStartWeekEnd &&
                        snapshot.ledgerVersion < ledger.version ||
                        snapshot.ledgerVersion == ledger.version &&
                        snapshot.projectionId == ledger.projectionId)
            }
        }
    }

    private fun saveGroupWeek(
        groupId: ObjectId,
        weekEnd: Instant,
        observedAt: Instant
    ): GroupWeeklyStatistics {
        repeat(MAX_CAS_ATTEMPTS) {
            val current = statisticsRepository.findGroupWeek(groupId, weekEnd)
            val replacement = GroupWeeklyStatistics(
                id = current?.id,
                groupId = groupId,
                weekEnd = weekEnd,
                counts = statisticsRepository.sumLatestMemberCounts(groupId, weekEnd),
                version = (current?.version ?: 0L) + 1L,
                updatedAt = observedAt
            )
            if (statisticsRepository.compareAndSetGroupWeek(current?.version, replacement)) {
                return replacement
            }
        }
        throw ConflictException(message = "그룹 통계가 동시에 갱신되어 다시 시도해야 합니다.")
    }

    private fun refresh(
        membership: StudyGroupMember,
        cutoff: Instant,
        observedAt: Instant
    ): MemberStatisticsDto {
        return refreshMemberStatistics.execute(
            RefreshMemberStatisticsCommand(
                groupId = membership.groupId.toHexString(),
                membershipId = membership.identifier,
                cutoff = cutoff,
                observedAt = observedAt
            )
        )
    }

    private fun requireManager(groupId: ObjectId, memberId: ObjectId): StudyGroupMember {
        val membership = requireActiveMember(groupId, memberId)
        if (!membership.isManager()) {
            throw ForbiddenException(message = "그룹 소유자 또는 관리자만 그룹 통계를 조회할 수 있습니다.")
        }
        return membership
    }

    private fun requireActiveMember(groupId: ObjectId, memberId: ObjectId): StudyGroupMember {
        return memberRepository.findByGroupIdAndMemberId(groupId, memberId)
            ?: throw NotFoundException(message = "해당 그룹의 멤버가 아닙니다.")
    }

    private fun StudyGroupMember.isManager(): Boolean {
        return role == GroupMemberRole.OWNER || role == GroupMemberRole.ADMIN
    }

    private companion object {
        const val WEEK_SECONDS = 7L * 24L * 60L * 60L
        const val INTERNAL_PAGE_SIZE = 200
        const val MAX_PAGE_SIZE = 100
        const val MAX_CAS_ATTEMPTS = 5
    }
}
