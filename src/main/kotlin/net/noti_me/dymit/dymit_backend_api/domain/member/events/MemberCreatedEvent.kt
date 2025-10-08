package net.noti_me.dymit.dymit_backend_api.domain.member.events

import net.noti_me.dymit.dymit_backend_api.common.event.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed

class MemberCreatedEvent(
    val member: Member
): PersonalFeedEvent(member) {

    override fun processUserFeed(): UserFeed {
        val member = this.source as Member
        return UserFeed(
            memberId = member.id!!,
            iconType = IconType.HAND_WAVING,
            messages = mutableListOf(
                FeedMessage(
                    text="환영합니다! ${member.nickname}님! Dymit에 오신 것을 환영합니다.",
                )
            ),
            associates = listOf()
        )
    }
}