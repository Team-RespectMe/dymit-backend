package net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto

import java.time.LocalDateTime

/**
 * 일별 회원 현황 조회 범위를 전달하는 명령입니다.
 *
 * @param startDate 조회 시작 시각
 * @param endDate 조회 종료 시각
 */
data class GetDailyMemberStatusCommand(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)
