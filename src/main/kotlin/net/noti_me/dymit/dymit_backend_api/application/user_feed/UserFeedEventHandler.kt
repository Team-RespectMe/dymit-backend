package net.noti_me.dymit.dymit_backend_api.application.user_feed

import net.noti_me.dymit.dymit_backend_api.application.push_notification.MemberPushEvent
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.event.CreateUserFeedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service



fun CreateUserFeedEvent.toPushEvent(): Any {
    return MemberPushEvent(
        memberId = this.memberId,
        title = "Dymit",
        body = this.content,
        image = this.image,
    )
}

@Service
class UserFeedEventHandler(
    private val userFeedRepository: UserFeedRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    @EventListener
    @Async
    fun handleUserFeedCreatedEvent(event: CreateUserFeedEvent) {
        val userFeed = toUserFeed(event)
        userFeedRepository.save(userFeed)
        if (event.requiredPush) {
            eventPublisher.publishEvent(event.toPushEvent())
        }
    }

    private fun toUserFeed(event: CreateUserFeedEvent): UserFeed {
        return UserFeed(
            memberId = event.memberId,
            message = event.content,
            associates = event.associates,
        )
    }
}