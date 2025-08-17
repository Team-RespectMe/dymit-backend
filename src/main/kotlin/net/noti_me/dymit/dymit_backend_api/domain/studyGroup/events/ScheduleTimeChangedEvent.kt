package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule.StudySchedule
import org.springframework.context.ApplicationEvent

class ScheduleTimeChangedEvent(
    val schedule: StudySchedule,
): ApplicationEvent(schedule) {

}