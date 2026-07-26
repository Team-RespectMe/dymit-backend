package net.noti_me.dymit.dymit_backend_api.push_notification.domain

import org.bson.types.ObjectId

/**
 * 스터디 그룹 회원들에게 전달할 푸시 메시지입니다.
 */
class GroupPushMessage(
    val groupId: ObjectId,
    val eventName: String,
    val title: String = "Dymit",
    val body: String,
    val image: String? = null,
    val data: Map<String, String> = emptyMap(),
    val excluded: MutableSet<ObjectId> = mutableSetOf()
)
