package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.application.push_notification.MemberPushEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.springframework.context.ApplicationEvent

class StudyRoleAssignedEvent(
    val schedule: StudySchedule,
    val role: ScheduleRole,
): ApplicationEvent(schedule){

    fun toUserFeed(
        group: StudyGroup,
        schedule: StudySchedule,
        member: StudyGroupMember,
    ): UserFeed {
        return UserFeed(
            memberId = member.memberId,
            message = "[${group.name}] 에서 ${schedule.session} 회차에 필요한 역할이 ${member.nickname}님께 할당되었어요.",
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

    fun toMemberPushEvent(group: StudyGroup): MemberPushEvent {
        return MemberPushEvent(
            memberId = role.memberId,
            title = group.name,
            body = "회원님께 중요한 역할이 할당되었어요. 확인해보세요!",
            image = null,
            data = mapOf(
                "type" to "STUDY_GROUP_SCHEDULE",
                "groupId" to schedule.groupId.toHexString(),
                "scheduleId" to schedule.id.toHexString()
            )
        )
    }
}