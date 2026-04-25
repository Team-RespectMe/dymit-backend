package net.noti_me.dymit.dymit_backend_api.application.admin.impl

import net.noti_me.dymit.dymit_backend_api.application.admin.dto.AdminPushNotificationCommand
import net.noti_me.dymit.dymit_backend_api.application.admin.usecases.SendPushUseCase
import net.noti_me.dymit.dymit_backend_api.application.push_notification.PushService
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class SendPushUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val pushService: PushService,
) : SendPushUseCase {

    override fun sendPushNotifications(command: AdminPushNotificationCommand) {
        command.memberIds.asSequence()
            .map { PersonalPushMessage(
                memberId = ObjectId(it),
                title = "Dymit",
                body = command.message,
                eventName = "admin_push_notification",
                data = emptyMap(),
                image = null
            ) }
            .forEach { pushService.sendPersonalPush(it) }
    }
}