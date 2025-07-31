package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events

import org.springframework.context.ApplicationEvent

class StudyGroupCreateEvent(
    val groupId: String,
    source: Any
): ApplicationEvent(source) {
}