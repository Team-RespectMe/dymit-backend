package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.application.push_notification.GroupBroadCastPushEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.springframework.context.ApplicationEvent

/**
 * 스터디 그룹 일정이 생성되는 경우 발행해야하는 이벤트
 * 이 이벤트를 발행하면, 해당 스터디 그룹의 모든 멤버들에게
 * 알림이 전송됩니다.
 */
class StudyScheduleCreatedEvent(
    val studySchedule: StudySchedule,
): ApplicationEvent(studySchedule) {

    fun toUserFeed(
        group: StudyGroup,
        schedule: StudySchedule,
        member: StudyGroupMember
    ): UserFeed {
        return UserFeed(
            memberId = member.memberId,
            message = "[${group.name}] 새로운 스터디 일정: ${schedule.title}",
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

    fun toGroupPushEvent(group: StudyGroup): GroupBroadCastPushEvent {
        return GroupBroadCastPushEvent(
            groupId = group.id!!,
            title = group.name,
            body = "새로운 스터디 일정이 생성되었어요",
            image = null,
            data = mapOf(
                "type" to "STUDY_GROUP",
                "groupId" to group.id.toHexString()
            )
        )
    }
}