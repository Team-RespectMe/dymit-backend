package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSessionBoundary
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetGroupMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetGroupStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetManagedGroupStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetManagedGroupStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.ManagedGroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.RefreshMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.StatisticsGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant

/** 개인·그룹·관리 목록 통계를 동일한 회차 갱신 경로로 처리합니다. */
@Service
class StatisticsQueryService(
    private val memberRepository: StudyGroupMemberRepository,
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val statisticsRepository: StatisticsRepository,
    private val scheduleSourcePort: ScheduleStatisticsSourcePort,
    private val refreshMemberStatistics: RefreshMemberStatisticsUseCase
) : GetMemberStatisticsUseCase,
    GetGroupStatisticsUseCase,
    GetGroupMemberStatisticsUseCase,
    GetManagedGroupStatisticsUseCase {

    override fun execute(command: GetMemberStatisticsCommand): MemberStatisticsDto {
        val groupId = ObjectId(command.groupId)
        val requester = requireActiveMember(groupId, ObjectId(command.requesterId))
        val target = requireActiveMember(groupId, ObjectId(command.memberId))
        if (requester.memberId != target.memberId && !requester.isManager()) {
            throw ForbiddenException(message = "본인 또는 그룹 관리자만 구성원 통계를 조회할 수 있습니다.")
        }
        val observedAt = Instant.now()
        val boundaries = loadBoundaries(listOf(groupId), observedAt)[groupId].orEmpty()
        return refresh(target, boundaries, observedAt)
    }

    override fun execute(command: GetGroupStatisticsCommand): GroupStatisticsDto {
        val groupId = ObjectId(command.groupId)
        requireManager(groupId, ObjectId(command.requesterId))
        val group = requireActiveGroup(command.groupId)
        val observedAt = Instant.now()
        val boundaries = loadBoundaries(listOf(groupId), observedAt)[groupId].orEmpty()
        val activeCount = memberRepository.countDistinctActiveMembers(listOf(groupId))[groupId] ?: 0L
        return refreshGroup(group.toStatisticsGroupDto(), groupId, boundaries, observedAt, activeCount)
    }

    override fun execute(command: GetGroupMemberStatisticsCommand): List<MemberStatisticsDto> {
        validateSize(command.size)
        val groupId = ObjectId(command.groupId)
        requireManager(groupId, ObjectId(command.requesterId))
        val memberships = memberRepository.findActiveByGroupId(
            groupId = groupId,
            cursor = command.cursor?.let(::ObjectId),
            limit = command.size + 1
        )
        val observedAt = Instant.now()
        val boundaries = loadBoundaries(listOf(groupId), observedAt)[groupId].orEmpty()
        return memberships.map { refresh(it, boundaries, observedAt) }
    }

    override fun execute(command: GetManagedGroupStatisticsCommand): List<ManagedGroupStatisticsDto> {
        validateSize(command.size)
        val observedAt = Instant.now()
        val groupIds = memberRepository.findManagedGroupIds(
            memberId = ObjectId(command.requesterId),
            cursor = command.cursor?.let(::ObjectId),
            limit = command.size + 1
        )
        val groupsById = loadActiveGroups(groupIds)
        val responseGroupIds = groupIds.take(command.size)
        val boundaries = loadBoundaries(responseGroupIds, observedAt)
        val activeCounts = memberRepository.countDistinctActiveMembers(responseGroupIds)
        val groupsHavingSchedule = scheduleSourcePort.loadGroupIdsHavingSchedule(responseGroupIds)
        val responses = responseGroupIds.map { groupId ->
            refreshGroup(
                group = requireNotNull(groupsById[groupId]),
                groupId = groupId,
                boundaries = boundaries[groupId].orEmpty(),
                observedAt = observedAt,
                activeMemberCount = activeCounts[groupId] ?: 0L
            ).toManaged(hasSchedule = groupId in groupsHavingSchedule)
        }.toMutableList()
        if (groupIds.size > command.size) {
            responses += ManagedGroupStatisticsDto(
                group = requireNotNull(groupsById[groupIds.last()]),
                latestSession = null,
                statisticsAt = null,
                activeMemberCount = 0L,
                taskSubmissionRate = 0.0,
                scheduleAttendanceRate = 0.0,
                hasSchedule = false
            )
        }
        return responses
    }

    private fun refreshGroup(
        group: StatisticsGroupDto,
        groupId: ObjectId,
        boundaries: List<StatisticsSessionBoundary>,
        observedAt: Instant,
        activeMemberCount: Long
    ): GroupStatisticsDto {
        if (boundaries.isEmpty()) {
            return emptyGroupDto(group, activeMemberCount)
        }
        repeat(MAX_CAS_ATTEMPTS) {
            refreshAllMemberships(groupId, boundaries, observedAt)
            val ledgers = statisticsRepository.findLedgersByGroupId(groupId)
            val sourceProjectionToken = sourceProjectionToken(ledgers)
            val storedByScheduleId = statisticsRepository.findGroupSessions(groupId)
                .associateBy { it.scheduleId }
                .toMutableMap()
            val latestStored = storedByScheduleId[boundaries.last().scheduleId]
            val rebuildAll = latestStored?.sourceProjectionToken != sourceProjectionToken
            var projectionComplete = true
            boundaries.forEach { boundary ->
                val stored = storedByScheduleId[boundary.scheduleId]
                val needsSave = rebuildAll || stored == null ||
                    stored.sourceProjectionToken != sourceProjectionToken ||
                    stored.scheduleAt != boundary.scheduleAt || stored.session != boundary.session
                if (needsSave && projectionComplete) {
                    val saved = saveGroupSession(
                        groupId = groupId,
                        boundary = boundary,
                        current = stored,
                        ledgers = ledgers,
                        sourceProjectionToken = sourceProjectionToken,
                        observedAt = observedAt
                    )
                    projectionComplete = saved != null
                    saved?.let { storedByScheduleId[boundary.scheduleId] = it }
                }
            }
            if (projectionComplete && sourceProjectionToken == sourceProjectionToken(groupId)) {
                return groupDto(group, boundaries, storedByScheduleId, activeMemberCount)
            }
        }
        throw ConflictException(message = "그룹 통계 원장 투영이 완료되지 않아 다시 시도해야 합니다.")
    }

    private fun groupDto(
        group: StatisticsGroupDto,
        boundaries: List<StatisticsSessionBoundary>,
        storedByScheduleId: Map<ObjectId, GroupSessionStatistics>,
        activeMemberCount: Long
    ): GroupStatisticsDto {
        val currentBoundary = boundaries.last()
        val previousBoundary = boundaries.getOrNull(boundaries.lastIndex - 1)
        val current = requireNotNull(storedByScheduleId[currentBoundary.scheduleId])
        val previous = previousBoundary?.let { storedByScheduleId[it.scheduleId] }
        return GroupStatisticsDto(
            group = group,
            latestSession = currentBoundary.session,
            previousSession = previousBoundary?.session,
            statisticsAt = currentBoundary.scheduleAt,
            activeMemberCount = activeMemberCount,
            counts = current.counts,
            taskSubmissionRate = current.counts.taskSubmissionRate(),
            scheduleAttendanceRate = current.counts.scheduleAttendanceRate(),
            previousTaskSubmissionRate = previous?.counts?.taskSubmissionRate() ?: 0.0,
            previousScheduleAttendanceRate = previous?.counts?.scheduleAttendanceRate() ?: 0.0
        )
    }

    private fun refreshAllMemberships(
        groupId: ObjectId,
        boundaries: List<StatisticsSessionBoundary>,
        observedAt: Instant
    ) {
        var cursor: ObjectId? = null
        do {
            val page = memberRepository.findByGroupIdIncludingDeleted(groupId, cursor, INTERNAL_PAGE_SIZE)
            page.forEach { refresh(it, boundaries, observedAt) }
            cursor = page.lastOrNull()?.id
        } while (page.size == INTERNAL_PAGE_SIZE)
    }

    private fun saveGroupSession(
        groupId: ObjectId,
        boundary: StatisticsSessionBoundary,
        current: GroupSessionStatistics?,
        ledgers: List<MemberSessionStatisticsLedger>,
        sourceProjectionToken: String,
        observedAt: Instant
    ): GroupSessionStatistics? {
        var expected = current
        repeat(MAX_CAS_ATTEMPTS) {
            val counts = statisticsRepository.sumMemberCounts(groupId, boundary.scheduleId, ledgers)
                ?: return null
            val replacement = GroupSessionStatistics(
                id = expected?.id,
                groupId = groupId,
                scheduleId = boundary.scheduleId,
                session = boundary.session,
                scheduleAt = boundary.scheduleAt,
                statisticsAt = boundary.scheduleAt,
                counts = counts,
                sourceProjectionToken = sourceProjectionToken,
                version = (expected?.version ?: 0L) + 1L,
                updatedAt = observedAt
            )
            if (statisticsRepository.compareAndSetGroupSession(expected?.version, replacement)) {
                return replacement
            }
            expected = statisticsRepository.findGroupSession(groupId, boundary.scheduleId)
        }
        throw ConflictException(message = "그룹 통계가 동시에 갱신되어 다시 시도해야 합니다.")
    }

    private fun refresh(
        membership: StudyGroupMember,
        boundaries: List<StatisticsSessionBoundary>,
        observedAt: Instant
    ): MemberStatisticsDto {
        return refreshMemberStatistics.execute(
            RefreshMemberStatisticsCommand(
                groupId = membership.groupId.toHexString(),
                membershipId = membership.identifier,
                boundaries = boundaries,
                observedAt = observedAt
            )
        )
    }

    private fun loadBoundaries(
        groupIds: List<ObjectId>,
        observedAt: Instant
    ): Map<ObjectId, List<StatisticsSessionBoundary>> {
        return scheduleSourcePort.loadBoundaries(groupIds, observedAt)
            .mapValues { (_, values) -> values.sortedWith(BOUNDARY_COMPARATOR) }
    }

    private fun sourceProjectionToken(groupId: ObjectId): String {
        return sourceProjectionToken(statisticsRepository.findLedgersByGroupId(groupId))
    }

    private fun sourceProjectionToken(
        ledgers: List<MemberSessionStatisticsLedger>
    ): String {
        val source = ledgers
            .sortedBy { it.membershipId }
            .joinToString("|") {
                "${it.membershipId.toHexString()}:${it.version}:${it.projectionId}"
            }
        return MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
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

    private fun GroupStatisticsDto.toManaged(hasSchedule: Boolean): ManagedGroupStatisticsDto {
        return ManagedGroupStatisticsDto(
            group = group,
            latestSession = latestSession,
            statisticsAt = statisticsAt,
            activeMemberCount = activeMemberCount,
            taskSubmissionRate = taskSubmissionRate,
            scheduleAttendanceRate = scheduleAttendanceRate,
            hasSchedule = hasSchedule
        )
    }

    private fun emptyGroupDto(group: StatisticsGroupDto, activeMemberCount: Long): GroupStatisticsDto {
        return GroupStatisticsDto(
            group = group,
            latestSession = null,
            previousSession = null,
            statisticsAt = null,
            activeMemberCount = activeMemberCount,
            counts = StatisticsCounts(),
            taskSubmissionRate = 0.0,
            scheduleAttendanceRate = 0.0,
            previousTaskSubmissionRate = 0.0,
            previousScheduleAttendanceRate = 0.0
        )
    }

    private fun requireActiveGroup(groupId: String): StudyGroup {
        return loadStudyGroupPort.loadByGroupId(groupId)
            ?.takeUnless { it.isDeleted }
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
    }

    private fun loadActiveGroups(groupIds: List<ObjectId>): Map<ObjectId, StatisticsGroupDto> {
        if (groupIds.isEmpty()) {
            return emptyMap()
        }
        val groups = loadStudyGroupPort.loadByGroupIds(groupIds.map(ObjectId::toHexString))
            .filterNot { it.isDeleted }
            .associateBy { requireNotNull(it.id) }
        if (groups.size != groupIds.size || groupIds.any { it !in groups }) {
            throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
        }
        return groups.mapValues { (_, group) -> group.toStatisticsGroupDto() }
    }

    private fun StudyGroup.toStatisticsGroupDto(): StatisticsGroupDto {
        return StatisticsGroupDto(id = identifier, name = name)
    }

    private fun validateSize(size: Int) {
        if (size !in 1..MAX_PAGE_SIZE) {
            throw BadRequestException(message = "size는 1 이상 $MAX_PAGE_SIZE 이하여야 합니다.")
        }
    }

    private companion object {
        const val INTERNAL_PAGE_SIZE = 200
        const val MAX_PAGE_SIZE = 100
        const val MAX_CAS_ATTEMPTS = 5
        val BOUNDARY_COMPARATOR = compareBy<StatisticsSessionBoundary>({ it.scheduleAt }, { it.scheduleId })
    }
}
