package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.admin.dto.SendAdminPushCommand

/**
 * 관리자 푸시 알림 요청입니다.
 */
@Schema(description = "관리자 푸시 알림 요청")
data class AdminPushNotificationRequest(
    val message: String,
    val receiverIds: List<String>
) {

    /**
     * 요청을 관리자 푸시 전송 명령으로 변환합니다.
     */
    fun toCommand(): SendAdminPushCommand {
        return SendAdminPushCommand(
            message = message,
            memberIds = receiverIds
        )
    }
}
