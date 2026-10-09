package net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import java.time.Instant

/** 구성원 누적 통계 DTO입니다. */
data class MemberStatisticsDto(
    val groupId: String,
    val membershipId: String,
    val memberId: String,
    val latestSession: Long?,
    val statisticsAt: Instant?,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double
)

/** 통계 응답에 포함할 현재 그룹 요약입니다. */
data class StatisticsGroupDto(
    val id: String,
    val name: String
)

/** 그룹 회차 통계 DTO입니다. */
data class GroupStatisticsDto(
    val group: StatisticsGroupDto,
    val latestSession: Long?,
    val previousSession: Long?,
    val statisticsAt: Instant?,
    val activeMemberCount: Long,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double,
    val previousTaskSubmissionRate: Double,
    val previousScheduleAttendanceRate: Double
)

/** 관리 중인 그룹의 통계 목록 항목입니다. */
data class ManagedGroupStatisticsDto(
    val group: StatisticsGroupDto,
    val latestSession: Long?,
    val statisticsAt: Instant?,
    val activeMemberCount: Long,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double,
    val hasSchedule: Boolean
)
