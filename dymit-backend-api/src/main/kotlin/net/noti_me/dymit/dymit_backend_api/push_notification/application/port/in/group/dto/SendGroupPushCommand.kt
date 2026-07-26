package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.dto

import org.bson.types.ObjectId

/**
 * 그룹 푸시 알림 전송 명령입니다.
 */
data class SendGroupPushCommand(
    val groupId: ObjectId,
    val eventName: String,
    val title: String,
    val body: String,
    val image: String?,
    val data: Map<String, String>,
    val excludedMemberIds: Set<ObjectId>
)
