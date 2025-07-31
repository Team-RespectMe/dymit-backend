package net.noti_me.dymit.dymit_backend_api.domain.member.events

import org.springframework.context.ApplicationEvent

class MemberProfileImageDeleteEvent(
    val filePath: String,
    source: Any
) : ApplicationEvent(source) {
}