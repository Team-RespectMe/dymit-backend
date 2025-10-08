package net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import java.time.LocalDateTime

class StudyScheduleSummaryDto(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String,
    val scheduleAt: LocalDateTime,
    val session:Long,
    val location: LocationVo,
    var participantCount: Long = 0L,
    val roles: List<ScheduleRoleDto> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {

        fun from(entity: StudySchedule): StudyScheduleSummaryDto {
            return StudyScheduleSummaryDto(
                id = entity.identifier,
                groupId = entity.groupId.toHexString(),
                title = entity.title,
                description = entity.description,
                scheduleAt = entity.scheduleAt,
                session = entity.session,
                location = LocationVo.from(entity.location),
                participantCount = entity.nrParticipant,
                roles = entity.roles.map { ScheduleRoleDto.from(it) },
                createdAt = entity.createdAt ?: LocalDateTime.now(),
                updatedAt = entity.updatedAt ?: LocalDateTime.now()
            )
        }

    }
}