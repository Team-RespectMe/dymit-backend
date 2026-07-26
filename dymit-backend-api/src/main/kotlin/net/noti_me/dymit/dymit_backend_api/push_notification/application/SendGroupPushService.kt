package net.noti_me.dymit.dymit_backend_api.push_notification.application

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.SendGroupPushUseCase
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.dto.SendGroupPushCommand
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.SendPushNotificationPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.dto.PushDeliveryDto
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.LoadPushMemberPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group.LoadPushGroupMemberPort
import org.springframework.stereotype.Service

/**
 * 그룹 푸시 알림 전송을 처리하는 애플리케이션 서비스입니다.
 */
@Service
class SendGroupPushService(
    private val loadPushGroupMemberPort: LoadPushGroupMemberPort,
    private val loadPushMemberPort: LoadPushMemberPort,
    private val sendPushNotificationPort: SendPushNotificationPort
) : SendGroupPushUseCase {

    /**
     * 제외 대상을 뺀 그룹 회원들의 활성 토큰으로 푸시 알림을 전송합니다.
     */
    override fun execute(command: SendGroupPushCommand) {
        val receiverIds = loadPushGroupMemberPort.loadByGroupId(command.groupId)
            .map { it.memberId }
            .filterNot { it in command.excludedMemberIds }

        val activeTokens = loadPushMemberPort.loadByIds(receiverIds)
            .flatMap { member -> member.deviceTokens.filter { it.isActive }.map { it.token } }
            .distinct()

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
