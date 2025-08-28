package net.noti_me.dymit.dymit_backend_api.application.push_notification

import org.bson.types.ObjectId
import org.springframework.context.ApplicationEvent

class GroupBroadCastPushEvent(
    val groupId: ObjectId,
    val title: String = "Dymit",
    val body: String,
    val image: String? = null,
    val data: Map<String, String> = emptyMap(),
) : ApplicationEvent(title) {
}

class SchedulePushEvent(
    val scheduleId: ObjectId,
    val title: String = "Dymit",
    val body: String,
    val image: String? = null,
    val data: Map<String, String> = emptyMap(),
) : ApplicationEvent(title) {

}

class MemberPushEvent(
    val memberId: ObjectId,
    val title: String = "Dymit",
    val body: String,
    val image: String? = null,
    val data: Map<String, String> = emptyMap(),
): ApplicationEvent(title) {

}