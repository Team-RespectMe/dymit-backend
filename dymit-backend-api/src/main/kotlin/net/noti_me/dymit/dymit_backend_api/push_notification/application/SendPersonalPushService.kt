package net.noti_me.dymit.dymit_backend_api.push_notification.application

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.SendPersonalPushUseCase
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.dto.SendPersonalPushCommand
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.SendPushNotificationPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.dto.PushDeliveryDto
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.LoadPushMemberPort
import org.springframework.stereotype.Service

/**
 * 개인 푸시 알림 전송을 처리하는 애플리케이션 서비스입니다.
 */
@Service
class SendPersonalPushService(
    private val loadPushMemberPort: LoadPushMemberPort,
    private val sendPushNotificationPort: SendPushNotificationPort
) : SendPersonalPushUseCase {

    /**
     * 활성 디바이스 토큰으로 개인 푸시 알림을 전송합니다.
     */
    override fun execute(command: SendPersonalPushCommand) {
        val member = loadPushMemberPort.loadById(command.memberId) ?: return
        val activeTokens = member.deviceTokens
            .filter { it.isActive }
            .map { it.token }

        if (activeTokens.isEmpty()) {
            return
        }

        sendPushNotificationPort.send(
            PushDeliveryDto(
                deviceTokens = activeTokens,
                title = command.title,
                body = command.body,
                image = command.image,
                data = command.data + mapOf("eventName" to command.eventName)
            )
        )
    }
}
