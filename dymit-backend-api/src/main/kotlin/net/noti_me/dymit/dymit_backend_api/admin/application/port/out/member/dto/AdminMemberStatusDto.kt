package net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member.dto

import java.time.Instant

/**
 * 관리자 모듈이 회원 현황 저장소에서 조회하는 데이터입니다.
 *
 * @param newMemberCount 신규 회원 수
 * @param activeMemberCount 활성 회원 수
 * @param leaveMemberCount 탈퇴 회원 수
 * @param totalMemberCount 전체 회원 수
 * @param createdAt 기록 생성 시각
 */
data class AdminMemberStatusDto(
    val newMemberCount: Long,
    val activeMemberCount: Long,
    val leaveMemberCount: Long,
    val totalMemberCount: Long,
    val createdAt: Instant
)
