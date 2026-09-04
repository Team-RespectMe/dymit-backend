package net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import java.time.Instant

class StudyScheduleDetailDto(
    val id: String,
    val session: Long,
    val title: String,
    val description: String,
    val scheduleAt: Instant,
    val location : LocationVo,
    var participants: List<StudyScheduleParticipantDto> = emptyList(),
    val roles: List<ScheduleRoleDto> = emptyList(),
    var attending: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {

    companion object {
        fun from(entity: StudySchedule, participant: ScheduleParticipant? = null): StudyScheduleDetailDto {
            return StudyScheduleDetailDto(
                id = entity.identifier,
                session = entity.session,
                title = entity.title,
                description = entity.description,
                scheduleAt = entity.scheduleAt,
                location = LocationVo.from(entity.location),
                roles = entity.roles.map { ScheduleRoleDto.from(it) },
                attending = participant?.let{ true } ?: false,
                createdAt = entity.createdAt?: Instant.now(),
                updatedAt = entity.updatedAt?: Instant.now()
            )
        }
    }
}
