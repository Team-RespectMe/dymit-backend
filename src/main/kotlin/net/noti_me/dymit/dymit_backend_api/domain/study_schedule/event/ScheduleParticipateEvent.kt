package net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event

import net.noti_me.dymit.dymit_backend_api.common.event.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

class ScheduleParticipateEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    val member: StudyGroupMember
): PersonalFeedEvent(schedule) {

    private val eventName = "PARTICIPATE_SCHEDULE"

    override fun processUserFeed(): UserFeed {
        return UserFeed(
            memberId = group.ownerId,
            messages = listOf(
                FeedMessage("${group.name} ${schedule.session}회차 일정에 ${member.nickname} 님이 참여하기로 했어요." )
            ),
            iconType = IconType.CHECK,
            eventName = eventName,
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
            ),
        )
    }
}
