package net.noti_me.dymit.dymit_backend_api.study_group.domain.events

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import org.springframework.context.ApplicationEvent

class GroupOwnerMissingEvent(
    val group: StudyGroup
): ApplicationEvent(group) {
}