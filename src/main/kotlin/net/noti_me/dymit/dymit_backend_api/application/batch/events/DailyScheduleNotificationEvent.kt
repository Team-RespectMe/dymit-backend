package net.noti_me.dymit.dymit_backend_api.application.batch.events

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.bson.types.ObjectId

class DailyScheduleNotificationEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    memberIds: List<ObjectId>
): BroadcastEvent(memberIds) {

    private val eventName = "DAILY_SCHEDULE_NOTIFICATION"

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

    override fun processUserFeeds(): List<UserFeed> {
        return memberIds.map { memberId ->
            UserFeed(
                memberId = memberId,
                iconType = IconType.DATE,
                eventName = eventName,
                messages = listOf(
                    FeedMessage(
                        text = "${group.name}",
                        highlightColor = "#FF821B",
                        textColor = "#FFF2E4"
                    ),
                    FeedMessage(
                        text = " 의 ",
                    ),
                    FeedMessage(
                        text = "${schedule.session} 회차 ",
                        highlightColor = "#FF821B",
                        textColor = "#FFF2E4"
                    ),
                    FeedMessage(
                        text = " 일정이 오늘 예정되어 있어요!",
                    )
                ),
                associates = listOf(
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP,
                        resourceId = group.id.toString()
                    ),
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP_SCHEDULE,
                        resourceId = schedule.id.toString()
                    ),
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP_OWNER,
                        resourceId = memberId.toString()
                    )
                )
            )
        }
    }
}

