package net.noti_me.dymit.dymit_backend_api.domain.push

import org.bson.types.ObjectId

data class PersonalPushMessage(
    val memberId: ObjectId,
    val eventName: String,
    val title: String = "Dymit",
    val body: String,
    val image: String?,
    val data: Map<String, String>
) {

}
