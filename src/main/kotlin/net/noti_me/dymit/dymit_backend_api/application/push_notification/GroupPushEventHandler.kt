package net.noti_me.dymit.dymit_backend_api.application.push_notification

import net.noti_me.dymit.dymit_backend_api.common.event.GroupPushEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class GroupPushEventHandler(
    private val pushService: PushService
) {

    @EventListener(classes = [GroupPushEvent::class])
    @Async
    fun handleGroupPushEvent(event: GroupPushEvent) {
        pushService.sendGroupPush(event.toGroupPush())
    }
}