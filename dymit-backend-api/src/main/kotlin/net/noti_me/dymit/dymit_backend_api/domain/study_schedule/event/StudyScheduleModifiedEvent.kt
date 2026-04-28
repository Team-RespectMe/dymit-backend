package net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event

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


class StudyScheduleModifiedEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    memberIds: List<ObjectId>
): BroadcastEvent(memberIds) {

    private val eventName = "STUDY_SCHEDULE_MODIFIED"

    override fun processPushMessages(): List<PersonalPushMessage> {
        return memberIds.map { memberId ->
            PersonalPushMessage(
                memberId = memberId,
                eventName = eventName,
                title = group.name,
                body = "${schedule.session}회차 일정이 변경되었어요!",
                image = null,
                data = mapOf(
                    "groupId" to group.identifier,
                    "scheduleId" to schedule.identifier,
                    "ownerId" to group.ownerId.toHexString()
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
                        text = group.name,
                    ),
                    FeedMessage(
                        text = " ${schedule.session}회차 ",
                    ),
                    FeedMessage(
                        text = " 일정이 변경되었어요!",
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
                    ),
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP_OWNER,
                        resourceId = group.ownerId.toHexString()
                    )
                )
            )
        }
    }
}

