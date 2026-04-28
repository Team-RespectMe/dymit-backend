package net.noti_me.dymit.dymit_backend_api.domain.user_feed.event

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import org.bson.types.ObjectId

class CreateUserFeedEvent(
    val memberId: ObjectId,
    val content: String,
    val image: String? = null,
    val associates: List<AssociatedResource> = emptyList(),
    val requiredPush: Boolean = false
) {
}