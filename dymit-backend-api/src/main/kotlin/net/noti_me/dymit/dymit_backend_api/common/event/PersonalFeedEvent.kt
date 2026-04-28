package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.common.event.Feedable
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.springframework.context.ApplicationEvent

abstract class PersonalFeedEvent(source: Any)
    : ApplicationEvent(source), Feedable {

    protected abstract fun processUserFeed(): UserFeed

    final override fun toUserFeed(): UserFeed {
        return processUserFeed()
    }
}