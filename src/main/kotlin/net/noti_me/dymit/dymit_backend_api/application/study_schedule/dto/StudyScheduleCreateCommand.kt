package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.RoleAssignment
import java.time.LocalDateTime

class StudyScheduleCreateCommand(
    val title: String,
    val description: String,
    val scheduleAt: LocalDateTime,
    val location: LocationVo,
    val roles: List<RoleAssignment>
) {
}