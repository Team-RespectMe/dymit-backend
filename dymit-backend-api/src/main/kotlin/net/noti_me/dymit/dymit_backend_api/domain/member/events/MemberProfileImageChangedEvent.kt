package net.noti_me.dymit.dymit_backend_api.domain.member.events

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import org.springframework.context.ApplicationEvent

/**
 * 멤버 프로필 이미지가 변경되었을 때 발행되는 이벤트
 * 이 이벤트는 도메인 이벤트로, Push나 Feed와는 관계가 없다.
 * 이 이벤트는 멤버 프로필을 임베딩하는 다른 도메인 엔티티가 일관성을 요구하는 경우 반드시 구독해야한다.
 * @param member 프로필 이미지가 변경된 멤버
 */
class MemberProfileImageChangedEvent(
    val member: Member
) : ApplicationEvent(member) {

}