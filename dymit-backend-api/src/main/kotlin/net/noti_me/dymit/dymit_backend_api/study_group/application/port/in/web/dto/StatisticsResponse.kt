package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.ManagedGroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.StatisticsGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import java.time.Instant

/** 구성원 누적 통계 응답입니다. */
class MemberStatisticsResponse(
    val groupId: String,
    val memberId: String,
    val latestSession: Long?,
    val statisticsAt: Instant?,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double
) : BaseResponse() {
    companion object {
        /** 애플리케이션 DTO를 응답으로 변환합니다. */
        fun from(dto: MemberStatisticsDto) = MemberStatisticsResponse(
            groupId = dto.groupId,
            memberId = dto.memberId,
            latestSession = dto.latestSession,
            statisticsAt = dto.statisticsAt,
            counts = dto.counts,
            taskSubmissionRate = dto.taskSubmissionRate,
            scheduleAttendanceRate = dto.scheduleAttendanceRate
        )
    }
}

/** 그룹의 최근·직전 진행 회차 누적 통계 응답입니다. */
class GroupStatisticsResponse(
    val group: StatisticsGroupResponse,
    val latestSession: Long?,
    val previousSession: Long?,
    val statisticsAt: Instant?,
    val activeMemberCount: Long,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double,
    val previousTaskSubmissionRate: Double,
    val previousScheduleAttendanceRate: Double
) : BaseResponse() {
    companion object {
        /** 애플리케이션 DTO를 응답으로 변환합니다. */
        fun from(dto: GroupStatisticsDto) = GroupStatisticsResponse(
            group = StatisticsGroupResponse.from(dto.group),
            latestSession = dto.latestSession,
            previousSession = dto.previousSession,
            statisticsAt = dto.statisticsAt,
            activeMemberCount = dto.activeMemberCount,
            counts = dto.counts,
            taskSubmissionRate = dto.taskSubmissionRate,
            scheduleAttendanceRate = dto.scheduleAttendanceRate,
            previousTaskSubmissionRate = dto.previousTaskSubmissionRate,
            previousScheduleAttendanceRate = dto.previousScheduleAttendanceRate
        )
    }
}

/** 관리 중인 그룹의 회차 통계 목록 항목입니다. */
class ManagedGroupStatisticsResponse(
    val group: StatisticsGroupResponse,
    val latestSession: Long?,
    val statisticsAt: Instant?,
    val activeMemberCount: Long,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double,
    val hasSchedule: Boolean
) : BaseResponse() {
    companion object {
        /** 애플리케이션 DTO를 응답으로 변환합니다. */
        fun from(dto: ManagedGroupStatisticsDto) = ManagedGroupStatisticsResponse(
            group = StatisticsGroupResponse.from(dto.group),
            latestSession = dto.latestSession,
            statisticsAt = dto.statisticsAt,
            activeMemberCount = dto.activeMemberCount,
            taskSubmissionRate = dto.taskSubmissionRate,
            scheduleAttendanceRate = dto.scheduleAttendanceRate,
            hasSchedule = dto.hasSchedule
        )
    }
}

/** 통계 응답의 현재 그룹 식별자와 이름입니다. */
class StatisticsGroupResponse(
    val id: String,
    val name: String
) {
    companion object {
        /** 애플리케이션 그룹 요약 DTO를 응답으로 변환합니다. */
        fun from(dto: StatisticsGroupDto) = StatisticsGroupResponse(
            id = dto.id,
            name = dto.name
        )
    }
}
