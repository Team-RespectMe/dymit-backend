package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import org.springframework.context.ApplicationEvent

abstract class GroupPushEvent(source: Any)
: ApplicationEvent(source), GroupPushable {

    protected abstract fun processGroupPush(): GroupPushMessage

    final override fun toGroupPush(): GroupPushMessage {
        return processGroupPush()
    }
}