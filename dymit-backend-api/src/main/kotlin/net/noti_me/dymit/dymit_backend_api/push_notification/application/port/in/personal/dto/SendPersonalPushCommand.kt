package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.dto

import org.bson.types.ObjectId

/**
 * 개인 푸시 알림 전송 명령입니다.
 */
data class SendPersonalPushCommand(
    val memberId: ObjectId,
    val eventName: String,
    val title: String,
    val body: String,
    val image: String?,
    val data: Map<String, String>
)
