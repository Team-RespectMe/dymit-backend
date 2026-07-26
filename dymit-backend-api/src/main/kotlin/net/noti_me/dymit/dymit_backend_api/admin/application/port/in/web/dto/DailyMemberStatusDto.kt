package net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto

import java.time.LocalDateTime

/**
 * 관리자에게 제공하는 일별 회원 현황입니다.
 *
 * @param newMemberCount 신규 회원 수
 * @param activeMemberCount 활성 회원 수
 * @param leaveMemberCount 탈퇴 회원 수
 * @param totalMemberCount 전체 회원 수
 * @param recordedAt 기록 시각
 */
data class DailyMemberStatusDto(
    val newMemberCount: Long,
    val activeMemberCount: Long,
    val leaveMemberCount: Long,
    val totalMemberCount: Long,
    val recordedAt: LocalDateTime
)
