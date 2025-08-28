package net.noti_me.dymit.dymit_backend_api.application.member

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.userFeed.UserFeedRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MemberEventHandler(
    private val userFeedRepository: UserFeedRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleMemberCreatedEvent(event: MemberCreatedEvent) {
        val member = event.source as Member
        val welcomeFeed = event.toUserFeed(member)
        userFeedRepository.save(welcomeFeed)
    }
}