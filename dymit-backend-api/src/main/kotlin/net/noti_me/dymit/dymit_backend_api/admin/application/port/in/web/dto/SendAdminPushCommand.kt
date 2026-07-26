package net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto

/**
 * 관리자가 선택한 회원들에게 푸시 알림을 전송하는 명령입니다.
 *
 * @param message 푸시 본문
 * @param memberIds 수신 회원 식별자 목록
 */
data class SendAdminPushCommand(
    val message: String,
    val memberIds: List<String>
)
