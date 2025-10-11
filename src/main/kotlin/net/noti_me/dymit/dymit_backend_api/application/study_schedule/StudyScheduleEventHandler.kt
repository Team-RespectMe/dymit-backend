package net.noti_me.dymit.dymit_backend_api.application.study_schedule

import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
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