package net.noti_me.dymit.dymit_backend_api.application.push_notification

import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import org.bson.types.ObjectId

interface PushService {

    fun sendPushNotification(
        deviceToken: String,
        title: String,
        body: String,
        image : String? = null,
        data: Map<String, String> = emptyMap(),
    )

    fun sendPushNotifications(
        deviceTokens: List<String>,
        title: String,
        body: String,
        image: String? = null,
        data: Map<String, String> = emptyMap(),
    )
}