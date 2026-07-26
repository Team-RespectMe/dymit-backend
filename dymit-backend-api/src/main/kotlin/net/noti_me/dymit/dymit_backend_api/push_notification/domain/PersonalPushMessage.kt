package net.noti_me.dymit.dymit_backend_api.push_notification.domain

import org.bson.types.ObjectId

/**
 * 한 회원에게 전달할 푸시 메시지입니다.
 */
data class PersonalPushMessage(
    val memberId: ObjectId,
    val eventName: String,
    val title: String = "Dymit",
    val body: String,
    val image: String?,
    val data: Map<String, String>
)
