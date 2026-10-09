package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourceData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSessionBoundary
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSourceQuery
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskAssignmentData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskStatisticsSourceData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.TaskStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.RefreshMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatistics
import net.noti_me.dymit.dymit_backend_api.study_group.domain.MemberSessionStatisticsLedger
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/** 구성원 통계 원장과 일정 회차별 누적 투영을 갱신합니다. */
@Service
class RefreshMemberStatisticsService(
    private val repository: StatisticsRepository,
    private val memberRepository: StudyGroupMemberRepository,
    private val taskSourcePort: TaskStatisticsSourcePort,
    private val scheduleSourcePort: ScheduleStatisticsSourcePort
) : RefreshMemberStatisticsUseCase {

    /** 고정된 진행 회차 목록과 관측 시각으로 통계를 갱신합니다. */
    override fun execute(command: RefreshMemberStatisticsCommand): MemberStatisticsDto {
        val membershipId = ObjectId(command.membershipId)
        val membership = memberRepository.findByIdIncludingDeleted(membershipId)
            ?: throw NotFoundException(message = "존재하지 않는 가입 관계입니다.")
        if (membership.groupId != ObjectId(command.groupId)) {
            throw NotFoundException(message = "해당 그룹의 가입 관계가 아닙니다.")
        }
        val boundaries = command.boundaries
            .distinctBy { it.scheduleId }
            .sortedWith(BOUNDARY_COMPARATOR)
        if (boundaries.isEmpty()) {
            return emptyDto(membership)
        }
        return refreshWithRetry(membership, boundaries, command.observedAt)
    }

    private fun refreshWithRetry(
        membership: StudyGroupMember,
        boundaries: List<StatisticsSessionBoundary>,
        observedAt: Instant
    ): MemberStatisticsDto {
        val membershipId = requireNotNull(membership.id)
        val joinedAt = requireNotNull(membership.createdAt)
        val target = boundaries.last()
        repeat(MAX_CAS_ATTEMPTS) {
            val current = repository.findLedger(membershipId)
            val existing = repository.findMemberSessions(membershipId)
            val currentIndex = current?.let { ledger ->
                boundaries.indexOfFirst { it.scheduleId == ledger.latestScheduleId }
            } ?: -1
            val validSnapshots = if (current == null) {
                emptyList()
            } else {
                existing.filter { isValid(it, current) }
            }
            val expectedPrefix = if (currentIndex >= 0) boundaries.take(currentIndex + 1) else emptyList()
            val storedPrefix = current?.let { ledger ->
                validSnapshots
                    .filter {
                        compareBoundary(
                            it.scheduleAt,
                            it.scheduleId,
                            ledger.latestScheduleAt,
                            ledger.latestScheduleId
                        ) <= 0
                    }
                    .sortedWith(compareBy({ it.scheduleAt }, { it.scheduleId }))
            }.orEmpty()
            val prefixComplete = current != null && currentIndex >= 0 &&
                current.latestScheduleAt == boundaries[currentIndex].scheduleAt &&
                current.latestSession == boundaries[currentIndex].session &&
                storedPrefix.size == expectedPrefix.size &&
                storedPrefix.zip(expectedPrefix).all { (snapshot, boundary) ->
                    snapshot.scheduleId == boundary.scheduleId &&
                        snapshot.scheduleAt == boundary.scheduleAt &&
                        snapshot.session == boundary.session
                }
            val sourceCutoff = membership.deletedAt?.coerceAtMost(target.scheduleAt) ?: target.scheduleAt
            val mutationThrough = membership.deletedAt?.coerceAtMost(observedAt) ?: observedAt
            val taskQuery = sourceQuery(
                membership = membership,
                joinedAt = joinedAt,
                previousCutoff = current?.taskCalculatedThrough.takeIf { prefixComplete },
                cutoff = sourceCutoff,
                mutationAfter = current?.taskMutationWatermark.takeIf { prefixComplete },
                mutationThrough = mutationThrough
            )
            val scheduleQuery = taskQuery.copy(
                previousCutoff = current?.scheduleCalculatedThrough.takeIf { prefixComplete },
                mutationAfter = current?.scheduleMutationWatermark.takeIf { prefixComplete }
            )
            val taskChanges = taskSourcePort.loadChanged(taskQuery)
            val scheduleChanges = scheduleSourcePort.loadChanged(scheduleQuery)
            val appended = if (prefixComplete) boundaries.drop(currentIndex + 1) else emptyList()
            val targetSnapshot = validSnapshots.firstOrNull { it.scheduleId == target.scheduleId }
            val sameTimeAppend = current != null && appended.firstOrNull()?.scheduleAt == current.latestScheduleAt
            val needsReplay = current == null || !prefixComplete || taskChanges.requiresReplay ||
                scheduleChanges.requiresReplay || sameTimeAppend ||
                appended.isEmpty() && (taskChanges.sources.isNotEmpty() || scheduleChanges.sources.isNotEmpty())

            if (!needsReplay && appended.isEmpty() && targetSnapshot != null) {
                return targetSnapshot.toDto()
            }

            val nextVersion = (current?.version ?: 0L) + 1L
            val projectionId = UUID.randomUUID().toString()
            val snapshots = if (needsReplay) {
                replay(
                    taskQuery = taskQuery.copy(previousCutoff = null, mutationAfter = null),
                    scheduleQuery = scheduleQuery.copy(previousCutoff = null, mutationAfter = null),
                    membership = membership,
                    boundaries = boundaries,
                    version = nextVersion,
                    projectionId = projectionId,
                    observedAt = observedAt
                )
            } else {
                append(
                    current = requireNotNull(current),
                    taskSources = taskChanges.sources,
                    scheduleSources = scheduleChanges.sources,
                    membership = membership,
                    previousBoundary = boundaries[currentIndex],
                    boundaries = appended,
                    version = nextVersion,
                    projectionId = projectionId,
                    observedAt = observedAt
                )
            }
            val projectionStartScheduleId = snapshots.firstOrNull()?.scheduleId ?: target.scheduleId
            val projectionStartAt = snapshots.firstOrNull()?.scheduleAt ?: target.scheduleAt
            val counts = snapshots.lastOrNull()?.counts ?: requireNotNull(current).counts
            val replacement = MemberSessionStatisticsLedger(
                id = current?.id,
                groupId = membership.groupId,
                membershipId = membershipId,
                memberId = membership.memberId,
                counts = counts,
                latestScheduleId = target.scheduleId,
                latestSession = target.session,
                latestScheduleAt = target.scheduleAt,
                taskCalculatedThrough = sourceCutoff,
                scheduleCalculatedThrough = sourceCutoff,
                taskMutationWatermark = mutationThrough,
                scheduleMutationWatermark = mutationThrough,
                version = nextVersion,
                projectionId = projectionId,
                projectionStartScheduleId = projectionStartScheduleId,
                projectionStartAt = projectionStartAt,
                updatedAt = observedAt
            )
            if (repository.compareAndSetLedger(current?.version, replacement)) {
                repository.saveMemberSessions(snapshots)
                return snapshots.lastOrNull()?.toDto() ?: replacement.toDto()
            }
        }
        throw ConflictException(message = "통계가 동시에 갱신되어 다시 시도해야 합니다.")
    }

    private fun replay(
        taskQuery: StatisticsSourceQuery,
        scheduleQuery: StatisticsSourceQuery,
        membership: StudyGroupMember,
        boundaries: List<StatisticsSessionBoundary>,
        version: Long,
        projectionId: String,
        observedAt: Instant
    ): List<MemberSessionStatistics> {
        val tasks = taskSourcePort.loadAll(taskQuery)
        val schedules = scheduleSourcePort.loadAll(scheduleQuery)
        return boundaries.map { boundary ->
            snapshot(
                membership = membership,
                boundary = boundary,
                counts = calculateCounts(tasks, schedules, membership, boundary),
                version = version,
                projectionId = projectionId,
                observedAt = observedAt
            )
        }
    }

    private fun append(
        current: MemberSessionStatisticsLedger,
        taskSources: List<TaskStatisticsSourceData>,
        scheduleSources: List<ScheduleStatisticsSourceData>,
        membership: StudyGroupMember,
        previousBoundary: StatisticsSessionBoundary,
        boundaries: List<StatisticsSessionBoundary>,
        version: Long,
        projectionId: String,
        observedAt: Instant
    ): List<MemberSessionStatistics> {
        val previousSourceCounts = calculateCounts(taskSources, scheduleSources, membership, previousBoundary)
        return boundaries.map { boundary ->
            snapshot(
                membership = membership,
                boundary = boundary,
                counts = current.counts +
                    calculateCounts(taskSources, scheduleSources, membership, boundary) -
                    previousSourceCounts,
                version = version,
                projectionId = projectionId,
                observedAt = observedAt
            )
        }
    }

    private fun snapshot(
        membership: StudyGroupMember,
        boundary: StatisticsSessionBoundary,
        counts: StatisticsCounts,
        version: Long,
        projectionId: String,
        observedAt: Instant
    ): MemberSessionStatistics {
        return MemberSessionStatistics(
            groupId = membership.groupId,
            membershipId = requireNotNull(membership.id),
            memberId = membership.memberId,
            scheduleId = boundary.scheduleId,
            session = boundary.session,
            scheduleAt = boundary.scheduleAt,
            statisticsAt = boundary.scheduleAt,
            counts = counts,
            ledgerVersion = version,
            projectionId = projectionId,
            updatedAt = observedAt
        )
    }

    private fun calculateCounts(
        tasks: List<TaskStatisticsSourceData>,
        schedules: List<ScheduleStatisticsSourceData>,
        membership: StudyGroupMember,
        boundary: StatisticsSessionBoundary
    ): StatisticsCounts {
        val joinedAt = requireNotNull(membership.createdAt)
        val leftAt = membership.deletedAt
        val stateAt = leftAt?.coerceAtMost(boundary.scheduleAt) ?: boundary.scheduleAt
        val eligibleTasks = tasks.mapNotNull { task ->
            val expireAt = task.expireAtAt(stateAt)
            val withinBoundary = expireAt <= boundary.scheduleAt
            val withinMembership = expireAt >= joinedAt && (leftAt == null || expireAt < leftAt)
            val validTask = task.taskDeletedAt == null || task.taskDeletedAt > stateAt
            val validSchedule = task.relatedScheduleDeletedAt == null || task.relatedScheduleDeletedAt > stateAt
            if (!withinBoundary || !withinMembership || !validTask || !validSchedule) {
                return@mapNotNull null
            }
            task.assignments
                .filter { it.assignedAt >= joinedAt && it.assignedAt <= stateAt }
                .filter { it.deletedAt == null || it.deletedAt > stateAt }
                .maxByOrNull { it.assignedAt }
        }
        val eligibleSchedules = schedules.filter { schedule ->
            schedule.createdAt >= joinedAt && schedule.scheduleAt >= joinedAt && includes(boundary, schedule) &&
                (leftAt == null || schedule.scheduleAt < leftAt) &&
                (schedule.scheduleDeletedAt == null || schedule.scheduleDeletedAt > stateAt)
        }
        return StatisticsCounts(
            submittedTaskCount = eligibleTasks.count { it.submittedAt(stateAt) }.toLong(),
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

    private fun includes(
        boundary: StatisticsSessionBoundary,
        schedule: ScheduleStatisticsSourceData
    ): Boolean {
        return schedule.scheduleAt < boundary.scheduleAt ||
            schedule.scheduleAt == boundary.scheduleAt && schedule.scheduleId <= boundary.scheduleId
    }

    private fun TaskStatisticsSourceData.expireAtAt(cutoff: Instant): Instant {
        val firstLaterChange = expireAtHistory.sortedBy { it.changedAt }
            .firstOrNull { it.changedAt > cutoff }
        return firstLaterChange?.previousExpireAt
            ?: expireAtHistory.withIndex()
                .maxWithOrNull(compareBy({ it.value.changedAt }, { it.index }))
                ?.value
                ?.expireAt
            ?: expireAt
    }

    private fun TaskAssignmentData.submittedAt(cutoff: Instant): Boolean {
        return statusHistory.withIndex()
            .filter { it.value.changedAt <= cutoff }
            .maxWithOrNull(compareBy({ it.value.changedAt }, { it.index }))
            ?.value
            ?.submitted
            ?: (legacySubmitted && legacyStatusUpdatedAt?.let { it <= cutoff } == true)
    }

    private fun sourceQuery(
        membership: StudyGroupMember,
        joinedAt: Instant,
        previousCutoff: Instant?,
        cutoff: Instant,
        mutationAfter: Instant?,
        mutationThrough: Instant
    ): StatisticsSourceQuery {
        return StatisticsSourceQuery(
            groupId = membership.groupId,
            memberId = membership.memberId,
            joinedAt = joinedAt,
            previousCutoff = previousCutoff,
            cutoff = cutoff,
            mutationAfter = mutationAfter,
            mutationThrough = mutationThrough
        )
    }

    private fun isValid(
        snapshot: MemberSessionStatistics,
        ledger: MemberSessionStatisticsLedger
    ): Boolean {
        val beforeProjection = compareBoundary(
            snapshot.scheduleAt,
            snapshot.scheduleId,
            ledger.projectionStartAt,
            ledger.projectionStartScheduleId
        ) < 0
        return beforeProjection && snapshot.ledgerVersion < ledger.version ||
            snapshot.ledgerVersion == ledger.version && snapshot.projectionId == ledger.projectionId
    }

    private fun MemberSessionStatistics.toDto(): MemberStatisticsDto {
        return MemberStatisticsDto(
            groupId = groupId.toHexString(),
            membershipId = membershipId.toHexString(),
            memberId = memberId.toHexString(),
            latestSession = session,
            statisticsAt = statisticsAt,
            counts = counts,
            taskSubmissionRate = counts.taskSubmissionRate(),
            scheduleAttendanceRate = counts.scheduleAttendanceRate()
        )
    }

    private fun MemberSessionStatisticsLedger.toDto(): MemberStatisticsDto {
        return MemberStatisticsDto(
            groupId = groupId.toHexString(),
            membershipId = membershipId.toHexString(),
            memberId = memberId.toHexString(),
            latestSession = latestSession,
            statisticsAt = latestScheduleAt,
            counts = counts,
            taskSubmissionRate = counts.taskSubmissionRate(),
            scheduleAttendanceRate = counts.scheduleAttendanceRate()
        )
    }

    private fun emptyDto(membership: StudyGroupMember): MemberStatisticsDto {
        return MemberStatisticsDto(
            groupId = membership.groupId.toHexString(),
            membershipId = membership.identifier,
            memberId = membership.memberId.toHexString(),
            latestSession = null,
            statisticsAt = null,
            counts = StatisticsCounts(),
            taskSubmissionRate = 0.0,
            scheduleAttendanceRate = 0.0
        )
    }

    private fun compareBoundary(
        leftAt: Instant,
        leftId: ObjectId,
        rightAt: Instant,
        rightId: ObjectId
    ): Int {
        val timeComparison = leftAt.compareTo(rightAt)
        return if (timeComparison != 0) timeComparison else leftId.compareTo(rightId)
    }

    private companion object {
        const val MAX_CAS_ATTEMPTS = 5
        val BOUNDARY_COMPARATOR = compareBy<StatisticsSessionBoundary>({ it.scheduleAt }, { it.scheduleId })
    }
}
