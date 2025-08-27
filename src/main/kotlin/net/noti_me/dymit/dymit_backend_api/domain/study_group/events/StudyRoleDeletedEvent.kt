package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import org.springframework.context.ApplicationEvent

class StudyRoleDeletedEvent(
    val schedule: StudySchedule,
    val role: ScheduleRole,
): ApplicationEvent(schedule) {
}