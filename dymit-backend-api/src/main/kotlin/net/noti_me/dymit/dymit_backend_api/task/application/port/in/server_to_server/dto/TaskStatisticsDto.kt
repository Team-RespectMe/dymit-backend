package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.server_to_server.dto

import org.bson.types.ObjectId
import java.time.Instant

/** 과제 통계 원천 조회 범위입니다. */
data class TaskStatisticsQuery(
    val groupId: ObjectId,
    val memberId: ObjectId,
    val joinedAt: Instant,
    val previousCutoff: Instant?,
    val cutoff: Instant,
    val mutationAfter: Instant?,
    val mutationThrough: Instant
)

/** 과제 제출 상태 이력 값입니다. */
data class TaskStatisticsStatusHistoryDto(val submitted: Boolean, val changedAt: Instant)

/** 과제 마감 변경 이력 값입니다. */
data class TaskStatisticsExpireAtHistoryDto(
    val previousExpireAt: Instant,
    val expireAt: Instant,
    val changedAt: Instant
)

/** 과제 할당 구간 값입니다. */
data class TaskStatisticsAssignmentDto(
    val assignedAt: Instant,
    val deletedAt: Instant?,
    val statusHistory: List<TaskStatisticsStatusHistoryDto>,
    val legacySubmitted: Boolean,
    val legacyStatusUpdatedAt: Instant?
)

/** 과제 통계 원천 값입니다. */
data class TaskStatisticsDto(
    val taskId: ObjectId,
    val expireAt: Instant,
    val expireAtHistory: List<TaskStatisticsExpireAtHistoryDto>,
    val deletedAt: Instant?,
    val relatedScheduleDeletedAt: Instant?,
    val assignments: List<TaskStatisticsAssignmentDto>
)

/** 과제 변경 조회 결과입니다. */
data class TaskStatisticsChanges(
    val sources: List<TaskStatisticsDto>,
    val requiresReplay: Boolean
)
