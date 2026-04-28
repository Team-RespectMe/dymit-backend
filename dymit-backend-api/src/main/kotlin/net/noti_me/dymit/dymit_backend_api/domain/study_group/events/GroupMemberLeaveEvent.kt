package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.common.event.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType.STUDY_GROUP
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

/**
 * 그룹 멤버가 탈퇴한 경우 발생하는 이벤트
 * 피드 서비스에서는 이 이벤트를 구독하여 그룹 관리자의 사용자 피드로 만든다.
 * @param member 탈퇴한 멤버
 * @param group 탈퇴한 멤버가 속했던 그룹
 */
class GroupMemberLeaveEvent(
    val member: StudyGroupMember,
    val group: StudyGroup
): PersonalFeedEvent(group) {

    private val eventName = "GROUP_MEMBER_LEAVE"

    override fun processUserFeed(): UserFeed {
        return UserFeed(
            memberId = group.ownerId,
            iconType = IconType.BAD,
            messages = listOf(
                FeedMessage(
                    text = "${member.nickname} 님이 ${group.name}에서 탈퇴하셨습니다."
                ),
            ),
            eventName = eventName,
            associates = listOf(
                AssociatedResource(
                    type = STUDY_GROUP,
                    resourceId = group.identifier
                )
            )
        )
    }
}
