package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.common.event.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

class GroupMemberJoinEvent(
    val group: StudyGroup,
    val member: StudyGroupMember
): PersonalFeedEvent(member) {

    private val eventName = "GROUP_MEMBER_JOIN"

    override fun processUserFeed(): UserFeed {
        return UserFeed(
            memberId = group.ownerId,
            messages = listOf(
                FeedMessage(text = "${member.nickname}님이 ${group.name}에 참가하셨습니다.")
            ),
            iconType = IconType.APPLAUSE,
            eventName = eventName,
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP,
                    resourceId = group.identifier
                )
            )
        )
    }
}
