package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events

import org.springframework.context.ApplicationEvent

class ScheduleLocationChangedEvent(
    val groupId: String,
    val scheduleId: String,
    val location: String,
    source: Any
): ApplicationEvent(source) {

}