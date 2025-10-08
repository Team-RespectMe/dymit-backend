package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import org.springframework.context.ApplicationEvent

abstract class PersonalPushEvent(source: Any)
: ApplicationEvent(source), Pushable {

    protected abstract fun processPushMessage(): PersonalPushMessage

    final override fun toPushMessage(): PersonalPushMessage {
        return processPushMessage()
    }
}