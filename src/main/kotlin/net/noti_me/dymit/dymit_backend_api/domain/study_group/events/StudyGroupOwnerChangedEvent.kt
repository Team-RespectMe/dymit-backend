package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import org.springframework.context.ApplicationEvent

class StudyGroupOwnerChangedEvent(
    val group: StudyGroup,
) : ApplicationEvent(group) {
}