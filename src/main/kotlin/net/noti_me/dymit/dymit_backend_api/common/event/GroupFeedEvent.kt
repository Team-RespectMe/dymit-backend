package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.common.event.GroupFeedable
import org.springframework.context.ApplicationEvent

abstract class GroupFeedEvent(source: Any)
: ApplicationEvent(source), GroupFeedable {

    abstract fun processGroupFeed(): GroupFeed

    final override fun toGroupFeed(): GroupFeed {
        return processGroupFeed()
    }
}