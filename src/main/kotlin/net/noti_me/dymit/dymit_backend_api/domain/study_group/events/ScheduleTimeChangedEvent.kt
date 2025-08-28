package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.application.push_notification.SchedulePushEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.springframework.context.ApplicationEvent

class ScheduleTimeChangedEvent(
    val schedule: StudySchedule,
): ApplicationEvent(schedule) {

    fun toUserFeed(
        group: StudyGroup,
        schedule: StudySchedule,
        member: StudyGroupMember
    ): UserFeed {
        return UserFeed(
            memberId = member.memberId,
            message = "[${group.name}] ${schedule.session} 회차 일정이 변경되었어요.",
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP,
                    resourceId = group.id.toHexString()
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_SCHEDULE,
                    resourceId = schedule.id.toHexString()
                )
            )
        )
    }

    fun toSchedulePushEvent(group: StudyGroup): SchedulePushEvent {
        return SchedulePushEvent(
            scheduleId = this.schedule.id,
            title = group.name,
            body = "${schedule.session} 회차 일정이 변경되었어요.",
            image = null,
            data = mapOf(
                "type" to "STUDY_GROUP_SCHEDULE",
                "groupId" to schedule.groupId.toHexString(),
                "scheduleId" to schedule.id.toHexString()
            )
        )
    }
}