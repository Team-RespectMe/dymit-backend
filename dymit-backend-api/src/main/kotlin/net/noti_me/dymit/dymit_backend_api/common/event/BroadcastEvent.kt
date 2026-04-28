package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEvent

abstract class BroadcastEvent(
    val memberIds: List<ObjectId>,
): ApplicationEvent(memberIds), BroadcastFeedable, BroadcastPushable {

    override fun toFeeds(): List<UserFeed> {
        return processUserFeeds()
    }

    override fun toPushMessages(): List<PersonalPushMessage> {
        return processPushMessages()
    }

    abstract fun processPushMessages() : List<PersonalPushMessage>

    abstract fun processUserFeeds(): List<UserFeed>
}

