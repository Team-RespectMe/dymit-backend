package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import org.springframework.context.ApplicationEvent

class StudyGroupOwnerChangedEvent(
    val studyGroupId: String,
    val previousOwnerId: String,
    val newOwnerId: String,
    source: Any
) : ApplicationEvent(source) {
}