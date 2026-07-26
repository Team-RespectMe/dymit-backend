package net.noti_me.dymit.dymit_backend_api.study_schedule.domain.event

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule

data class StudyRoleAssignedEvent(
    val group: StudyScheduleGroupDto,
    val schedule: StudySchedule,
    val role: ScheduleRole
)

data class StudyRoleChangedEvent(
    val group: StudyScheduleGroupDto,
    val schedule: StudySchedule,
    val role: ScheduleRole
)

data class StudyRoleDeletedEvent(
    val group: StudyScheduleGroupDto,
    val schedule: StudySchedule,
    val role: ScheduleRole
)
