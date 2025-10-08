package net.noti_me.dymit.dymit_backend_api.domain.push

import org.bson.types.ObjectId

class GroupPushMessage(
    val groupId: ObjectId,
    val title: String = "Dymit",
    val body: String,
    val image: String? = null,
    val data: Map<String, String> = emptyMap()
) {
}