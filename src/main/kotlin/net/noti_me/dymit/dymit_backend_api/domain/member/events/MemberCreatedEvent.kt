package net.noti_me.dymit.dymit_backend_api.domain.member.events

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.springframework.context.ApplicationEvent

class MemberCreatedEvent(
    source: Member
): ApplicationEvent(source) {

    fun toUserFeed(member: Member): UserFeed {
        return UserFeed(
            memberId = member.id,
            message = "환영합니다! ${member.nickname}님! Dymit에 오신 것을 환영합니다.",
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.MEMBER,
                    resourceId = member.id.toHexString()
                )
            )
        )
    }
}