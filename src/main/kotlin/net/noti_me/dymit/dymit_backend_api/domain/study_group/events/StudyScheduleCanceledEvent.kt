package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.application.push_notification.SchedulePushEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.springframework.context.ApplicationEvent

/**
 * 스터디 그룹 일정이 취소되는 경우 발행해야하는 이벤트
 * 이 이벤트를 발행하면, 해당 스터디 그룹의 모든 멤버들에게
 * 알림이 전송됩니다.
 */
class StudyScheduleCanceledEvent(
    val group: StudyGroup,
    val schedule: StudySchedule
) : ApplicationEvent(schedule) {

    fun toUserFeed(
        group: StudyGroup,
        schedule: StudySchedule,
        participant: ScheduleParticipant,
    ): UserFeed {
        return UserFeed(
            memberId = participant.memberId,
            message = "[${group.name}] ${schedule.session} 회차 일정이 취소되었어요.",
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP,
                    resourceId = group.identifier
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_SCHEDULE,
                    resourceId = schedule.identifier
                )
            )
        )
    }

    fun toSchedulePushEvent(): SchedulePushEvent {
        return SchedulePushEvent(
            scheduleId = schedule.id!!,
            title = group.name,
            body = "${schedule.session} 회차 일정이 취소되었어요.",
            image = null,
            data = mapOf(
                "type" to "STUDY_GROUP_SCHEDULE",
                "groupId" to group.identifier,
                "scheduleId" to schedule.identifier
            )
        )
    }
}