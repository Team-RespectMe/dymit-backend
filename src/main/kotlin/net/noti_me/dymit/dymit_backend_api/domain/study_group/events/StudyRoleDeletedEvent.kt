package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.common.event.PersonalImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

/**
 * 스터디 그룹 일정에서 역할이 해제된 경우 발생하는 이벤트
 * 피드 서비스에서는 이 이벤트를 구독하여 해당 멤버의 사용자 피드로 만들고, Push 알림을 발행한다..
 * @param group 역할이 해제된 스터디 그룹
 * @param schedule 역할이 해제된 일정
 * @param role 해제된 역할
 */
class StudyRoleDeletedEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    val role: ScheduleRole,
): PersonalImportantEvent(role) {

    override fun processUserFeed(): UserFeed {
        return UserFeed(
            iconType = IconType.FRONT_OF_LAPTOP,
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
                    text = " 역할이 해제되었어요.",
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
            body = "${group.name} ${schedule.session}회차에 맡은 역할이 해제되었어요!",
            data = mapOf (
                "groupId" to schedule.groupId.toHexString(),
                "scheduleId" to schedule.identifier,
            ),
            image = null,
        )
    }
}