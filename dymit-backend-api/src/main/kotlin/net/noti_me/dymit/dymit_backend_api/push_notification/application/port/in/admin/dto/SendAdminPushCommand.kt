package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.admin.dto

/**
 * 관리자가 선택한 회원들에게 푸시 알림을 전송하는 명령입니다.
 */
data class SendAdminPushCommand(
    val message: String,
    val memberIds: List<String>
)
