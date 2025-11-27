package net.noti_me.dymit.dymit_backend_api.application.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
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
}