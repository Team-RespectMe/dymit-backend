package net.noti_me.dymit.dymit_backend_api.application.admin.usecases

import net.noti_me.dymit.dymit_backend_api.application.admin.dto.AdminPushNotificationCommand

interface SendPushUseCase {

    fun sendPushNotifications(
        command: AdminPushNotificationCommand
    )
}