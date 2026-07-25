package net.noti_me.dymit.dymit_backend_api.member.domain.events

import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import org.springframework.context.ApplicationEvent

class MemberDeletedEvent(
    val member: Member
): ApplicationEvent(member) {

}

