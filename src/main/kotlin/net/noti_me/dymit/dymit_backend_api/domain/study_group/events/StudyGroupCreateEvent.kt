package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import org.springframework.context.ApplicationEvent

class StudyGroupCreateEvent(
    val groupId: String,
    source: Any
): ApplicationEvent(source) {
}