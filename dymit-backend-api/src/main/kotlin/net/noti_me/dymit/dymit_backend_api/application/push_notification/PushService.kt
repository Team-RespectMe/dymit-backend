package net.noti_me.dymit.dymit_backend_api.application.push_notification

import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import org.bson.types.ObjectId

interface PushService {

    fun sendPersonalPush(message: PersonalPushMessage)

    fun sendGroupPush(message: GroupPushMessage)
}