package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto.RoleAssignment
import java.time.LocalDateTime

class StudyScheduleUpdateCommand(
    val title: String,
    val description: String,
    val scheduleAt: LocalDateTime,
    val location: LocationVo,
    val roles: List<RoleAssignment>,
) {
}