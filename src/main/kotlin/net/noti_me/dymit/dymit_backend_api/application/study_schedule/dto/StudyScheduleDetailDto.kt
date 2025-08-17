package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule.StudySchedule
import java.time.LocalDateTime

class StudyScheduleDetailDto(
    val id: String,
    val session: Long,
    val title: String,
    val description: String,
    val scheduleAt: LocalDateTime,
    val location : LocationVo,
    var participants: List<StudyScheduleParticipantDto> = emptyList(),
    val roles: List<ScheduleRoleDto> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun from(entity: StudySchedule): StudyScheduleDetailDto {
            return StudyScheduleDetailDto(
                id = entity.identifier,
                session = entity.session,
                title = entity.title,
                description = entity.description,
                scheduleAt = entity.scheduleAt,
                location = LocationVo.from(entity.location),
                roles = entity.roles.map { ScheduleRoleDto.from(it) },
                createdAt = entity.createdAt?: LocalDateTime.now(),
                updatedAt = entity.updatedAt?: LocalDateTime.now()
            )
        }
    }
}