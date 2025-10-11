package net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType

/**
 * 스터디 그룹 일정이 취소되는 경우 발행해야하는 이벤트
 * 이 이벤트를 발행하면, 해당 스터디 그룹의 모든 멤버들에게
 * 알림이 전송됩니다.
 */
class StudyScheduleCanceledEvent(
    val group: StudyGroup,
    val schedule: StudySchedule
): GroupImportantEvent(schedule) {

    override fun processGroupFeed(): GroupFeed {
        return GroupFeed(
            groupId = group.id!!,
            iconType = IconType.DATE,
            messages = listOf(
                FeedMessage(
                    text = group.name,
                ),
                FeedMessage(
                    text = " ${schedule.session}회차 ",
                ),
                FeedMessage(
                    text = " 일정이 취소되었어요!",
                )
            ),
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

    override fun processGroupPush(): GroupPushMessage {
        return GroupPushMessage(
            groupId = group.id!!,
            title = group.name,
            body = "${schedule.session}회차 일정이 취소되었어요!",
            data = mapOf(
                "groupId" to group.identifier,
                "scheduleId" to schedule.identifier
            ),
            image = null,
        )
    }
}