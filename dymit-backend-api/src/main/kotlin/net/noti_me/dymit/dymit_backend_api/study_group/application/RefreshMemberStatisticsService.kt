package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourceData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSourceQuery
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskAssignmentData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskStatisticsSourceData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.RefreshMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberWeeklyStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * 구성원 통계 원장과 누적 주간 스냅샷을 갱신합니다.
 */
@Service
class RefreshMemberStatisticsService(
    private val repository: StatisticsRepository,
    private val memberRepository: StudyGroupMemberRepository,
    private val taskSourcePort: TaskStatisticsSourcePort,
    private val scheduleSourcePort: ScheduleStatisticsSourcePort
) : RefreshMemberStatisticsUseCase {

    /**
     * 고정된 업무 상한과 관측 시각으로 통계를 갱신합니다.
     */
    override fun execute(command: RefreshMemberStatisticsCommand): MemberStatisticsDto {
        val membershipId = ObjectId(command.membershipId)
        val membership = memberRepository.findByIdIncludingDeleted(membershipId)
            ?: throw NotFoundException(message = "존재하지 않는 가입 관계입니다.")
        if (membership.groupId != ObjectId(command.groupId)) {
            throw NotFoundException(message = "해당 그룹의 가입 관계가 아닙니다.")
        }
        val joinedAt = requireNotNull(membership.createdAt)
        val targetCutoff = membership.deletedAt
            ?.let(StatisticsWeekWindow::firstWeekEndAfter)
            ?.coerceAtMost(command.cutoff)
            ?: command.cutoff

        repeat(MAX_CAS_ATTEMPTS) {
            val current = repository.findLedger(membershipId)
            val weekEnds = StatisticsWeekWindow.weekEnds(joinedAt, targetCutoff)
            val existingWeeks = repository.findMemberWeeks(membershipId, targetCutoff)
            if (current != null && targetCutoff < current.taskCalculatedThrough) {
                throw BadRequestException(message = "최신 통계 원장을 과거 상한으로 되돌릴 수 없습니다.")
            }
            val missingWeeks = weekEnds.filter { weekEnd ->
                existingWeeks.none { snapshot ->
                    snapshot.weekEnd == weekEnd && current != null &&
                        (snapshot.weekEnd < current.projectionStartWeekEnd &&
                            snapshot.ledgerVersion < current.version ||
                            snapshot.ledgerVersion == current.version &&
                            snapshot.projectionId == current.projectionId)
                }
            }
            val taskQuery = StatisticsSourceQuery(
                groupId = membership.groupId,
                memberId = membership.memberId,
                joinedAt = joinedAt,
                previousCutoff = current?.taskCalculatedThrough,
                cutoff = targetCutoff,
                mutationAfter = current?.taskMutationWatermark,
                mutationThrough = command.observedAt
            )
            val scheduleQuery = taskQuery.copy(
                previousCutoff = current?.scheduleCalculatedThrough,
                mutationAfter = current?.scheduleMutationWatermark
            )
            val taskChanges = taskSourcePort.loadChanged(taskQuery)
            val scheduleChanges = scheduleSourcePort.loadChanged(scheduleQuery)
            val needsReplay = current == null || taskChanges.requiresReplay ||
                scheduleChanges.requiresReplay || missingWeeks.any { it <= current.taskCalculatedThrough }

            if (current != null && targetCutoff == current.taskCalculatedThrough &&
                targetCutoff == current.scheduleCalculatedThrough && !needsReplay &&
                missingWeeks.isEmpty() && taskChanges.sources.isEmpty() &&
                scheduleChanges.sources.isEmpty()) {
                return current.toDto(targetCutoff)
            }

            val nextVersion = (current?.version ?: 0L) + 1L
            val projectionId = UUID.randomUUID().toString()
            val snapshots = if (needsReplay) {
                replaySnapshots(
                    taskQuery = taskQuery,
                    scheduleQuery = scheduleQuery,
                    membershipId = membershipId,
                    groupId = membership.groupId,
                    memberId = membership.memberId,
                    joinedAt = joinedAt,
                    leftAt = membership.deletedAt,
                    weekEnds = weekEnds,
                    version = nextVersion,
                    projectionId = projectionId,
                    observedAt = command.observedAt
                )
            } else {
                incrementalSnapshots(
                    current = requireNotNull(current),
                    taskSources = taskChanges.sources,
                    scheduleSources = scheduleChanges.sources,
                    membershipId = membershipId,
                    groupId = membership.groupId,
                    memberId = membership.memberId,
                    joinedAt = joinedAt,
                    leftAt = membership.deletedAt,
                    weekEnds = weekEnds.filter { it > current.taskCalculatedThrough },
                    version = nextVersion,
                    projectionId = projectionId,
                    observedAt = command.observedAt
                )
            }
            val counts = snapshots.lastOrNull()?.counts ?: current?.counts ?: StatisticsCounts()
            val projectionStartWeekEnd = snapshots.firstOrNull()?.weekEnd
                ?: targetCutoff.plusSeconds(WEEK_SECONDS)
            val replacement = MemberStatisticsLedger(
                id = current?.id,
                groupId = membership.groupId,
                membershipId = membershipId,
                memberId = membership.memberId,
                counts = counts,
                taskCalculatedThrough = targetCutoff,
                scheduleCalculatedThrough = targetCutoff,
                taskMutationWatermark = command.observedAt,
                scheduleMutationWatermark = command.observedAt,
                version = nextVersion,
                projectionId = projectionId,
                projectionStartWeekEnd = projectionStartWeekEnd,
                updatedAt = command.observedAt
            )

            if (repository.compareAndSetLedger(current?.version, replacement)) {
                repository.saveMemberWeeks(snapshots)
                return replacement.toDto(targetCutoff)
            }
        }
        throw ConflictException(message = "통계가 동시에 갱신되어 다시 시도해야 합니다.")
    }

    private fun replaySnapshots(
        taskQuery: StatisticsSourceQuery,
        scheduleQuery: StatisticsSourceQuery,
        membershipId: ObjectId,
        groupId: ObjectId,
        memberId: ObjectId,
        joinedAt: Instant,
        leftAt: Instant?,
        weekEnds: List<Instant>,
        version: Long,
        projectionId: String,
        observedAt: Instant
    ): List<MemberWeeklyStatistics> {
        val tasks = taskSourcePort.loadAll(taskQuery)
        val schedules = scheduleSourcePort.loadAll(scheduleQuery)
        return weekEnds.map { weekEnd ->
            MemberWeeklyStatistics(
                groupId = groupId,
                membershipId = membershipId,
                memberId = memberId,
                weekEnd = weekEnd,
                counts = calculateCounts(tasks, schedules, joinedAt, leftAt, weekEnd),
                ledgerVersion = version,
                projectionId = projectionId,
                updatedAt = observedAt
            )
        }
    }

    private fun incrementalSnapshots(
        current: MemberStatisticsLedger,
        taskSources: List<TaskStatisticsSourceData>,
        scheduleSources: List<ScheduleStatisticsSourceData>,
        membershipId: ObjectId,
        groupId: ObjectId,
        memberId: ObjectId,
        joinedAt: Instant,
        leftAt: Instant?,
        weekEnds: List<Instant>,
        version: Long,
        projectionId: String,
        observedAt: Instant
    ): List<MemberWeeklyStatistics> {
        val previousSourceCounts = calculateCounts(
            taskSources,
            scheduleSources,
            joinedAt,
            leftAt,
            current.taskCalculatedThrough
        )
        return weekEnds.map { weekEnd ->
            MemberWeeklyStatistics(
                groupId = groupId,
                membershipId = membershipId,
                memberId = memberId,
                weekEnd = weekEnd,
                counts = current.counts +
                    calculateCounts(taskSources, scheduleSources, joinedAt, leftAt, weekEnd) -
                    previousSourceCounts,
                ledgerVersion = version,
                projectionId = projectionId,
                updatedAt = observedAt
            )
        }
    }

    private fun calculateCounts(
        tasks: List<TaskStatisticsSourceData>,
        schedules: List<ScheduleStatisticsSourceData>,
        joinedAt: Instant,
        leftAt: Instant?,
        cutoff: Instant
    ): StatisticsCounts {
        val sourceStateCutoff = leftAt?.coerceAtMost(cutoff) ?: cutoff
        val eligibleTasks = tasks.mapNotNull { task ->
            val expireAt = task.expireAtAt(sourceStateCutoff)
            val withinMembership = expireAt < cutoff && (leftAt == null || expireAt < leftAt)
            val validTask = task.taskDeletedAt == null || !task.taskDeletedAt.isBefore(sourceStateCutoff)
            val validSchedule = task.relatedScheduleDeletedAt == null ||
                !task.relatedScheduleDeletedAt.isBefore(sourceStateCutoff)
            if (!withinMembership || !validTask || !validSchedule) {
                return@mapNotNull null
            }
            val assignment = task.assignments
                .filter { it.assignedAt >= joinedAt && it.assignedAt < sourceStateCutoff }
                .filter { it.deletedAt == null || !it.deletedAt.isBefore(sourceStateCutoff) }
                .maxByOrNull { it.assignedAt }
                ?: return@mapNotNull null
            assignment
        }
        val eligibleSchedules = schedules.filter { schedule ->
            schedule.createdAt >= joinedAt && schedule.scheduleAt < cutoff &&
                (leftAt == null || schedule.scheduleAt < leftAt) &&
                (schedule.scheduleDeletedAt == null || !schedule.scheduleDeletedAt.isBefore(sourceStateCutoff))
        }
        return StatisticsCounts(
            submittedTaskCount = eligibleTasks.count { it.submittedAt(sourceStateCutoff) }.toLong(),
            assignedTaskCount = eligibleTasks.size.toLong(),
            attendedScheduleCount = eligibleSchedules.count { schedule ->
                schedule.participations.any { participation ->
                    participation.participatedAt <= schedule.scheduleAt &&
                        (participation.deletedAt == null || participation.deletedAt > schedule.scheduleAt)
                }
            }.toLong(),
            eligibleScheduleCount = eligibleSchedules.size.toLong()
        )
    }

    private fun TaskStatisticsSourceData.expireAtAt(cutoff: Instant): Instant {
        val firstLaterChange = expireAtHistory.sortedBy { it.changedAt }
            .firstOrNull { it.changedAt >= cutoff }
        return firstLaterChange?.previousExpireAt
            ?: expireAtHistory.withIndex()
                .maxWithOrNull(compareBy({ it.value.changedAt }, { it.index }))
                ?.value
                ?.expireAt
            ?: expireAt
    }

    private fun TaskAssignmentData.submittedAt(cutoff: Instant): Boolean {
        return statusHistory.withIndex()
            .filter { it.value.changedAt < cutoff }
            .maxWithOrNull(compareBy({ it.value.changedAt }, { it.index }))
            ?.value
            ?.submitted
            ?: (legacySubmitted && legacyStatusUpdatedAt?.let { it < cutoff } == true)
    }

    private fun MemberStatisticsLedger.toDto(weekEnd: Instant): MemberStatisticsDto {
        return MemberStatisticsDto(
            groupId = groupId.toHexString(),
            membershipId = membershipId.toHexString(),
            memberId = memberId.toHexString(),
            weekEnd = weekEnd,
            counts = counts,
            taskSubmissionRate = counts.taskSubmissionRate(),
            scheduleAttendanceRate = counts.scheduleAttendanceRate()
        )
    }

    private companion object {
        const val MAX_CAS_ATTEMPTS = 5
        const val WEEK_SECONDS = 7L * 24L * 60L * 60L
    }
}
