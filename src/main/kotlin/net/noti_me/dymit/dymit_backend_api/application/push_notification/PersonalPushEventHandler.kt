package net.noti_me.dymit.dymit_backend_api.application.push_notification

import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushable
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalPushEvent
import net.noti_me.dymit.dymit_backend_api.common.event.Pushable
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class PersonalPushEventHandler(
    private val pushService: PushService
) {

    @EventListener(classes = [PersonalPushEvent::class, PersonalImportantEvent::class])
    fun handlePersonalPushEvent(event: Pushable) {
        pushService.sendPersonalPush(event.toPushMessage())
    }

    @EventListener(classes = [GroupPushEvent::class, GroupImportantEvent::class])
    fun handleGroupPushEvent(event: GroupPushable) {
        pushService.sendGroupPush(event.toGroupPush())
    }
}