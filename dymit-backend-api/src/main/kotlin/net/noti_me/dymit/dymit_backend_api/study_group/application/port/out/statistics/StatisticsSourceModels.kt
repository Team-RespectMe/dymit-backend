package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics

import org.bson.types.ObjectId
import java.time.Instant

/**
 * 통계 원천 조회 범위입니다.
 */
data class StatisticsSourceQuery(
    val groupId: ObjectId,
    val memberId: ObjectId,
    val joinedAt: Instant,
    val previousCutoff: Instant?,
    val cutoff: Instant,
    val mutationAfter: Instant?,
    val mutationThrough: Instant
)

/**
 * 변경 조회 결과와 전체 재계산 필요 여부입니다.
 */
data class StatisticsSourceChanges<T>(
    val sources: List<T>,
    val requiresReplay: Boolean
)

/**
 * 과제 제출 상태 이력 값입니다.
 */
data class TaskStatusHistoryData(
    val submitted: Boolean,
    val changedAt: Instant
)

/** 과제 마감 시각 변경 이력 값입니다. */
data class TaskExpireAtHistoryData(
    val previousExpireAt: Instant,
    val expireAt: Instant,
    val changedAt: Instant
)

/** 과제 할당 구간과 제출 상태 값입니다. */
data class TaskAssignmentData(
    val assignedAt: Instant,
    val deletedAt: Instant?,
    val statusHistory: List<TaskStatusHistoryData>,
    val legacySubmitted: Boolean,
    val legacyStatusUpdatedAt: Instant?
)

/**
 * 통계 계산에 필요한 과제 원천 값입니다.
 */
data class TaskStatisticsSourceData(
    val taskId: ObjectId,
    val expireAt: Instant,
    val expireAtHistory: List<TaskExpireAtHistoryData>,
    val taskDeletedAt: Instant?,
    val relatedScheduleDeletedAt: Instant?,
    val assignments: List<TaskAssignmentData>
)

/** 일정 참여 구간 값입니다. */
data class ScheduleParticipationData(
    val participatedAt: Instant,
    val deletedAt: Instant?
)

/**
 * 통계 계산에 필요한 일정 원천 값입니다.
 */
data class ScheduleStatisticsSourceData(
    val scheduleId: ObjectId,
    val createdAt: Instant,
    val scheduleAt: Instant,
    val scheduleDeletedAt: Instant?,
    val participations: List<ScheduleParticipationData>
)
