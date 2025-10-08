package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.event.CreateUserFeedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class StudyGroupEventHandler(
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val DEFAULT_BATCH_SIZE = 50
    }
}