package net.noti_me.dymit.dymit_backend_api.application.feed

import net.noti_me.dymit.dymit_backend_api.application.feed.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.common.event.Feedable
import net.noti_me.dymit.dymit_backend_api.common.event.GroupFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.GroupFeedable
import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FeedEventHandler(
    private val userFeedService: UserFeedService,
    private val groupFeedService: GroupFeedService
) {

    @EventListener(classes = [PersonalFeedEvent::class, PersonalImportantEvent::class])
    @Async
    fun handleFeedableEvent(event: Feedable) {
        userFeedService.createUserFeed(userFeed=event.toUserFeed())
    }

    @EventListener(classes = [GroupFeedEvent::class, GroupImportantEvent::class])
    @Async
    fun handleGroupFeedableEvent(event: GroupFeedable) {
        groupFeedService.createGroupFeed(CreateGroupFeedCommand(
            groupFeed = event.toGroupFeed()
        ))
    }
}