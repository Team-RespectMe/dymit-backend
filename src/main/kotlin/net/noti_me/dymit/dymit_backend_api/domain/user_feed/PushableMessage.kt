package net.noti_me.dymit.dymit_backend_api.domain.user_feed

import net.noti_me.dymit.dymit_backend_api.application.push_notification.dto.PushMessage


interface PushableMessage {

    fun toPushMessage(): PushMessage
}