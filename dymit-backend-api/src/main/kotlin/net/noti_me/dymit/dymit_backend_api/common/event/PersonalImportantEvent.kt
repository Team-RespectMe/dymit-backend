package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.common.event.Feedable
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.springframework.context.ApplicationEvent

abstract class PersonalImportantEvent(
    source: Any
): ApplicationEvent(source) , Pushable, Feedable
{
    protected abstract fun processUserFeed(): UserFeed

    protected abstract fun processPushMessage(): PersonalPushMessage

    final override fun toUserFeed(): UserFeed {
        return processUserFeed()
    }

    final override fun toPushMessage(): PersonalPushMessage {
        return processPushMessage()
    }
}