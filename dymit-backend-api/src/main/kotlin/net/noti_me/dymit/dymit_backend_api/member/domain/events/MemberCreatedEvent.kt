package net.noti_me.dymit.dymit_backend_api.member.domain.events

import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import org.springframework.context.ApplicationEvent

/**
 * 회원 생성 후 환영 피드를 발행하는 이벤트입니다.
 *
 * @param member 생성된 회원
 */
class MemberCreatedEvent(
    val member: Member
) : ApplicationEvent(member), PersonalFeedEvent {

    private val eventName = "MEMBER_CREATED"

    /**
     * 환영 개인 피드 데이터를 반환합니다.
     *
     * @return 환영 피드 데이터
     */
    override fun toPersonalFeedData(): List<PersonalFeedEventData> {
        return listOf(
            PersonalFeedEventData(
                memberId = member.id!!.toHexString(),
                iconType = FeedEventIconType.APPLAUSE,
                eventName = eventName,
                messages = listOf(
                    FeedEventMessage(
                        text = "환영합니다! ${member.nickname}님! Dymit에 오신 것을 환영합니다."
                    )
                ),
                resources = emptyList()
            )
        )
    }
}
