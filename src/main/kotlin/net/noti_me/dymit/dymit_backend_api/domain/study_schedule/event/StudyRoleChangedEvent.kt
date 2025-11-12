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

/**
 * 스터디 그룹 일정에서 역할이 변경된 경우 발생하는 이벤트
 * 피드 서비스에서는 이 이벤트를 구독하여 해당 멤버의 사용자 피드
 * 로 만들고, Push 알림을 발행한다..
 * @param group 역할이 변경된 스터디 그룹
 * @param schedule 역할이 변경된 일정
 * @param role 변경된 역할
 */
class StudyRoleChangedEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    val role: ScheduleRole
): PersonalImportantEvent(role) {

    private val eventName = "STUDY_ROLE_CHANGED"

    override fun processPushMessage(): PersonalPushMessage {
        return PersonalPushMessage(
            memberId = role.memberId,
            title = "Dymit",
            eventName = eventName,
            body = "${group.name} ${schedule.session}회차 맡은 역할이 변경되었어요!",
            data = mapOf (
                "groupId" to schedule.groupId.toHexString(),
                "scheduleId" to schedule.identifier,
                "ownerId" to group.ownerId.toHexString()
            ),
            image = null,
        )
    }

    override fun processUserFeed(): UserFeed {
        return UserFeed(
            iconType = IconType.ROLE,
            eventName = eventName,
            memberId = role.memberId,
            messages = listOf(
                FeedMessage(
                    text = "${group.name} ${schedule.session}회차 맡은 역할이 변경되었어요!",
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
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_OWNER,
                    resourceId = group.ownerId.toHexString()
                )
            )
        )
    }
}
