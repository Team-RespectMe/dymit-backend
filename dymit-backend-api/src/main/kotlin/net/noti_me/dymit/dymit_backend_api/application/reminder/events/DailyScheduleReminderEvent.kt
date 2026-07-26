package net.noti_me.dymit.dymit_backend_api.application.reminder.events

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import org.bson.types.ObjectId

class DailyScheduleReminderEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    memberIds: List<ObjectId>
): BroadcastEvent(memberIds) {

    private val eventName = "DAILY_SCHEDULE_REMINDER"

    override fun processPushMessages(): List<PersonalPushMessage> {
        return memberIds.map { memberId ->
            PersonalPushMessage(
                memberId = memberId,
                eventName = eventName,
                body = "${group.name} 의 ${schedule.session} 회차 스터디가 오늘 예정되어 있어요!",
                image = group.profileImage.thumbnail,
                data = mapOf(
                    "groupId" to group.id.toString(),
                    "scheduleId" to schedule.id.toString(),
                    "ownerId" to group.ownerId.toString()
                )
            )
        }
    }

    override fun processPersonalFeedData(): List<PersonalFeedEventData> {
        return memberIds.map { memberId ->
            PersonalFeedEventData(
                memberId = memberId.toHexString(),
                iconType = FeedEventIconType.DATE,
                eventName = eventName,
                messages = listOf(
                    FeedEventMessage(
                        text = "${group.name} ${schedule.session}회차 일정이 오늘 예정되어 있어요!",
                    )
                ),
                resources = listOf(
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP,
                        resourceId = group.id.toString()
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP_SCHEDULE,
                        resourceId = schedule.id.toString()
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP_OWNER,
                        resourceId = memberId.toString()
                    )
                )
            )
        }
    }
}
