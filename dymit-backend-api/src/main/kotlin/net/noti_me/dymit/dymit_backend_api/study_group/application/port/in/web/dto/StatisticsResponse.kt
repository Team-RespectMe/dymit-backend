package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto

import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import java.time.Instant

/** 구성원 누적 통계 응답입니다. */
class MemberStatisticsResponse(
    val groupId: String,
    val memberId: String,
    val weekEnd: Instant,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double
) : BaseResponse() {
    companion object {
        /** 애플리케이션 DTO를 응답으로 변환합니다. */
        fun from(dto: MemberStatisticsDto) = MemberStatisticsResponse(
            groupId = dto.groupId,
            memberId = dto.memberId,
            weekEnd = dto.weekEnd,
            counts = dto.counts,
            taskSubmissionRate = dto.taskSubmissionRate,
            scheduleAttendanceRate = dto.scheduleAttendanceRate
        )
    }
}

/** 그룹 누적 통계와 직전 주 비율 응답입니다. */
class GroupStatisticsResponse(
    val groupId: String,
    val weekEnd: Instant,
    val counts: StatisticsCounts,
    val taskSubmissionRate: Double,
    val scheduleAttendanceRate: Double,
    val previousTaskSubmissionRate: Double,
    val previousScheduleAttendanceRate: Double
) : BaseResponse() {
    companion object {
        /** 애플리케이션 DTO를 응답으로 변환합니다. */
        fun from(dto: GroupStatisticsDto) = GroupStatisticsResponse(
            groupId = dto.groupId,
            weekEnd = dto.weekEnd,
            counts = dto.counts,
            taskSubmissionRate = dto.taskSubmissionRate,
            scheduleAttendanceRate = dto.scheduleAttendanceRate,
            previousTaskSubmissionRate = dto.previousTaskSubmissionRate,
            previousScheduleAttendanceRate = dto.previousScheduleAttendanceRate
        )
    }
}
