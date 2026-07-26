package net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 관리자 푸시 알림 요청입니다.
 *
 * @param message 푸시 본문
 * @param receiverIds 수신 회원 식별자 목록
 */
@Schema(description = "관리자 푸시 알림 요청")
data class AdminPushNotificationRequest(
    val message: String,
    val receiverIds: List<String>
) {

    /**
     * 요청을 관리자 푸시 전송 명령으로 변환합니다.
     *
     * @return 관리자 푸시 전송 명령
     */
    fun toCommand(): SendAdminPushCommand {
        return SendAdminPushCommand(
            message = message,
            memberIds = receiverIds
        )
    }
}
