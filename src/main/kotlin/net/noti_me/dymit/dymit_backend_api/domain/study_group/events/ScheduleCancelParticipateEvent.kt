package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.common.event.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

class ScheduleCancelParticipateEvent(
    val group: StudyGroup,
    val schedule: StudySchedule,
    val member: StudyGroupMember
): PersonalFeedEvent(schedule) {

    override fun processUserFeed(): UserFeed {
        return UserFeed(
            memberId = group.ownerId,
            messages = listOf(
                FeedMessage(
                    text = "스터디 그룹 ${group.name}의 ${schedule.session}회차 일정에 ${member.nickname} 님이 참여하지 않기로 했어요."
                )
            ),
            iconType = IconType.BAD,
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP,
                    resourceId = group.identifier
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_SCHEDULE,
                    resourceId = schedule.identifier
                )
            ),
        )
    }
}