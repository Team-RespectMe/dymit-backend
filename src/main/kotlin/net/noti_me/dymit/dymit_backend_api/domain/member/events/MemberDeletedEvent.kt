package net.noti_me.dymit.dymit_backend_api.domain.member.events

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import org.springframework.context.ApplicationEvent

class MemberDeletedEvent(
    val member: Member
): ApplicationEvent(member) {

}

