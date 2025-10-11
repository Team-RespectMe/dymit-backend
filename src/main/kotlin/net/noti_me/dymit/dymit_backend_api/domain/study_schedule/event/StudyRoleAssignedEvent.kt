package net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event

import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

class StudyRoleAssignedEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    val role: ScheduleRole,
): PersonalImportantEvent(role) {

    override fun processUserFeed(): UserFeed {
        return UserFeed(
            iconType = IconType.ROLE,
            memberId = role.memberId,
            messages = listOf(
                FeedMessage(
                    text = "${group.name} ${schedule.session}회차 ",
                ),
                FeedMessage(
                    text = role.roles.joinToString (", "),
                    textColor = "#FF821B",
                    highlightColor = "#FFF2E4"
                ),
                FeedMessage(
                    text = " 역할이 지정되었습니다.",
                ),
            ),
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP,
                    resourceId = schedule.groupId.toHexString()
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_SCHEDULE,
                    resourceId = schedule.identifier,
                )
            )
        )
    }

    override fun processPushMessage(): PersonalPushMessage {
        return PersonalPushMessage(
            memberId = role.memberId,
            title = "Dymit",
            body = "${group.name} ${schedule.session}회차에 새로운 역할이 부여되었어요!",
            data = mapOf (
                "groupId" to schedule.groupId.toHexString(),
                "scheduleId" to schedule.identifier,
            ),
            image = null,
        )
    }
}