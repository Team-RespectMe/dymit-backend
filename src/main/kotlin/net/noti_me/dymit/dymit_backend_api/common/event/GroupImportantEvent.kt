package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import org.springframework.context.ApplicationEvent

abstract class GroupImportantEvent(source: Any)
: ApplicationEvent(source), GroupPushable, GroupFeedable {

    protected abstract fun processGroupFeed(): GroupFeed

    protected abstract fun processGroupPush(): GroupPushMessage

    final override fun toGroupFeed(): GroupFeed {
        return processGroupFeed()
    }

    final override fun toGroupPush(): GroupPushMessage {
        return processGroupPush()
    }
}