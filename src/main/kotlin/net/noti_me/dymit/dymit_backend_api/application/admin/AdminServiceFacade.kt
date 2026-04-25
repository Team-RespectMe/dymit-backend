package net.noti_me.dymit.dymit_backend_api.application.admin

import net.noti_me.dymit.dymit_backend_api.application.admin.dto.AdminPushNotificationCommand
import net.noti_me.dymit.dymit_backend_api.application.admin.usecases.SendPushUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import org.springframework.stereotype.Service

@Service
class AdminServiceFacade(
    private val sendPushUseCase: SendPushUseCase,
) {

    fun sendPushNotifications(admin: MemberInfo, command: AdminPushNotificationCommand) {
        sendPushUseCase.sendPushNotifications(command)
    }
}