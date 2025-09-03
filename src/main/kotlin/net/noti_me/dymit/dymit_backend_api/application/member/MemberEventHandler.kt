package net.noti_me.dymit.dymit_backend_api.application.member

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageDeleteEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MemberEventHandler(
    private val userFeedRepository: UserFeedRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleMemberCreatedEvent(event: MemberCreatedEvent) {
        val member = event.source as Member
        val welcomeFeed = event.toUserFeed(member)
        userFeedRepository.save(welcomeFeed)
    }

    @EventListener
    @Async
    fun handleMemberImageDeleteEvent(event: MemberProfileImageDeleteEvent) {
        val filePath = event.filePath
        logger.info("Handling MemberProfileImageDeleteEvent for filePath: $filePath")
        // TODO: 실제 파일 업로드 서비스를 이용하여 오브젝트 삭제 로직 구현 필요
        logger.info("Deleted profile image at path: $filePath")
    }
}