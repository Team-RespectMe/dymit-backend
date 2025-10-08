package net.noti_me.dymit.dymit_backend_api.application.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.ScheduleTimeChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyRoleAssignedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCanceledEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
@Async
class StudyScheduleEventHandler(
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val studyGroupRepository: LoadStudyGroupPort,
    private val participantRepository: ScheduleParticipantRepository,
    private val userFeedRepository: UserFeedRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)
}