package net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import java.time.Instant

/** 구성원 누적 통계 DTO입니다. */
data class MemberStatisticsDto(
    val groupId: String,
    val membershipId: String,
    val memberId: String,
    val weekEnd: Instant,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double
)

/** 그룹 주간 통계 DTO입니다. */
data class GroupStatisticsDto(
    val groupId: String,
    val weekEnd: Instant,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double,
    val previousTaskSubmissionRate: Double,
    val previousScheduleAttendanceRate: Double
)
