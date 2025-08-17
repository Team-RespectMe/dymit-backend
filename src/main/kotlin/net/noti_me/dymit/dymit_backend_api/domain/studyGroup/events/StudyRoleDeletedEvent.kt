package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule.StudySchedule
import org.springframework.context.ApplicationEvent

class StudyRoleDeletedEvent(
    val schedule: StudySchedule,
    val role: ScheduleRole,
): ApplicationEvent(schedule) {
}