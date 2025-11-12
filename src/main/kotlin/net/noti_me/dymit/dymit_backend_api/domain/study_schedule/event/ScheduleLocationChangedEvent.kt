package net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event

import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType.DATE
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType.STUDY_GROUP

class ScheduleLocationChangedEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
): GroupImportantEvent(schedule) {

    private val eventName = "SCHEDULE_LOCATION_CHANGED"

    override fun processGroupFeed(): GroupFeed {
        return GroupFeed(
            groupId = schedule.groupId,
            iconType = DATE,
            eventName = eventName,
            messages = listOf(
                FeedMessage(
                    text = group.name,
                ),
                FeedMessage(
                    text = " ${schedule.session}회차 ",
                ),
                FeedMessage(
                    text = " 일정 장소가 변경되었어요!",
                )
            ),
            associates = listOf(
                AssociatedResource(
                    type = STUDY_GROUP,
                    resourceId = group.identifier
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_SCHEDULE,
                    resourceId = schedule.identifier
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_OWNER,
                    resourceId = group.ownerId.toHexString()
                )
            )
        )
    }

    override fun processGroupPush(): GroupPushMessage {
        return GroupPushMessage(
            groupId = schedule.groupId,
            title = group.name,
            body = "${schedule.session}회차 일정 장소가 변경되었어요!",
            eventName = eventName,
            data = mapOf(
                "groupId" to group.identifier,
                "scheduleId" to schedule.identifier,
                "ownerId" to group.ownerId.toHexString()
            ),
            image = null,
        )
    }
}
