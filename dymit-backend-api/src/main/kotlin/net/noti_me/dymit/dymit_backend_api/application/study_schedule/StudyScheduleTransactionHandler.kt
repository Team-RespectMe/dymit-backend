package net.noti_me.dymit.dymit_backend_api.application.study_schedule

import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.member.domain.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
@Async
class StudyScheduleTransactionHandler(
    private val scheduleCommentRepository: ScheduleCommentRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener(classes = [MemberNicknameChangedEvent::class])
    fun handleMemberNicknameChangedEvent(event: MemberNicknameChangedEvent) {
        val member = event.member
        scheduleCommentRepository.updateWriterInfo(member)
    }

    @Async
    @EventListener(classes = [MemberProfileImageChangedEvent::class])
    fun handleMemberProfileImageChangedEvent(event: MemberProfileImageChangedEvent) {
        val member = event.member
        scheduleCommentRepository.updateWriterInfo(member)
    }
}