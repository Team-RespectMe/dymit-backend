package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType.DATE
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType

class ScheduleTimeChangedEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
): GroupImportantEvent(schedule) {

    override fun processGroupFeed(): GroupFeed {
        return GroupFeed(
            groupId = schedule.groupId,
            iconType = DATE,
            messages = listOf(
                FeedMessage(
                    text = "${group.name} ${schedule.session}회차 모임 시간이 변경되었어요!",
                ),
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
            groupId = schedule.groupId,
            title = group.name,
            body = "${schedule.session}회차 모임 시간이 변경되었어요!",
            data = mapOf(
                "groupId" to group.identifier,
                "scheduleId" to schedule.identifier
            ),
            image = null,
        )
    }
}