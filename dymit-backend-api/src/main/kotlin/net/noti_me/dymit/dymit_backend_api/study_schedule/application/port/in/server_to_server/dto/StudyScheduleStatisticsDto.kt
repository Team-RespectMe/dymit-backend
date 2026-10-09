package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto

import org.bson.types.ObjectId
import java.time.Instant

/** 통계 회차를 식별하는 일정 경계입니다. */
data class StudyScheduleStatisticsBoundaryDto(
    val groupId: ObjectId,
    val scheduleId: ObjectId,
    val session: Long,
    val scheduleAt: Instant
)

/** 일정 통계 원천 조회 범위입니다. */
data class StudyScheduleStatisticsQuery(
    val groupId: ObjectId,
    val memberId: ObjectId,
    val joinedAt: Instant,
    val previousCutoff: Instant?,
    val cutoff: Instant,
    val mutationAfter: Instant?,
    val mutationThrough: Instant
)

/** 일정 참여 이력 값입니다. */
data class StudyScheduleParticipationStatisticsDto(
    val participatedAt: Instant,
    val deletedAt: Instant?
)

/** 일정 통계 원천 값입니다. */
data class StudyScheduleStatisticsDto(
    val scheduleId: ObjectId,
    val session: Long,
    val createdAt: Instant,
    val scheduleAt: Instant,
    val deletedAt: Instant?,
    val participations: List<StudyScheduleParticipationStatisticsDto>
)

/** 일정 변경 조회 결과입니다. */
data class StudyScheduleStatisticsChanges(
    val sources: List<StudyScheduleStatisticsDto>,
    val requiresReplay: Boolean
)

/** 과제 통계가 그룹 범위와 일정 취소를 판별하는 일정 참조 값입니다. */
data class StudyScheduleStatisticsReferenceDto(
    val scheduleId: ObjectId,
    val deletedAt: Instant?,
    val updatedAt: Instant?
)
