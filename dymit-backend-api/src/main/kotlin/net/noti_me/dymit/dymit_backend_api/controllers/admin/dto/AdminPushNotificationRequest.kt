package net.noti_me.dymit.dymit_backend_api.controllers.admin.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.admin.dto.AdminPushNotificationCommand
import java.util.UUID

@Schema(description = "관리자 푸시 알림 요청")
data class AdminPushNotificationRequest(
    val message: String,
    val receiverIds: List<String>
) {

    fun toCommand(): AdminPushNotificationCommand {
        return AdminPushNotificationCommand(
            message = this.message,
            memberIds = receiverIds
        )
    }
}
