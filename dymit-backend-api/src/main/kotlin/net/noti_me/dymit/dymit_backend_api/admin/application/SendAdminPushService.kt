package net.noti_me.dymit.dymit_backend_api.admin.application

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.SendAdminPushCommand
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.AdminPushNotificationPort
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.dto.AdminPushNotificationDto
import net.noti_me.dymit.dymit_backend_api.admin.application.usecase.SendAdminPushUseCase
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 관리자 푸시 알림 요청을 개인 푸시 전송으로 분배합니다.
 */
@Service
class SendAdminPushService(
    private val adminPushNotificationPort: AdminPushNotificationPort
) : SendAdminPushUseCase {

    /**
     * 요청에 포함된 각 회원에게 기존 관리자 메시지 형식으로 전송합니다.
     */
    override fun execute(command: SendAdminPushCommand) {
        command.memberIds.forEach { memberId ->
            adminPushNotificationPort.send(
                AdminPushNotificationDto(
                    memberId = ObjectId(memberId),
                    title = "Dymit",
                    body = command.message,
                    eventName = "admin_push_notification",
                    data = emptyMap(),
                    image = null
                )
            )
        }
    }
}
