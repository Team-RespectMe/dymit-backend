package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import org.springframework.context.ApplicationEvent

class StudyRoleChangedEvent(
    val schedule: StudySchedule,
    val role: ScheduleRole
) : ApplicationEvent(role){

}