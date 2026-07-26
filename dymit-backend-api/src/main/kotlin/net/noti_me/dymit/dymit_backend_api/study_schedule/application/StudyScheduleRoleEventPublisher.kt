package net.noti_me.dymit.dymit_backend_api.study_schedule.application

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventRoleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleEventScheduleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleAssignedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleChangedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleRoleDeletedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.event.StudyRoleAssignedEvent
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.event.StudyRoleChangedEvent
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.event.StudyRoleDeletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class StudyScheduleRoleEventPublisher(
    private val eventPublisher: ApplicationEventPublisher
) {

    @EventListener
    fun onAssigned(event: StudyRoleAssignedEvent) {
        eventPublisher.publishEvent(
            StudyScheduleRoleAssignedEventDto(
                group = event.group.toEventDto(),
                schedule = event.schedule.toEventDto(),
                role = event.role.toEventDto()
            )
        )
    }

    @EventListener
    fun onChanged(event: StudyRoleChangedEvent) {
        eventPublisher.publishEvent(
            StudyScheduleRoleChangedEventDto(
                group = event.group.toEventDto(),
                schedule = event.schedule.toEventDto(),
                role = event.role.toEventDto()
            )
        )
    }

    @EventListener
    fun onDeleted(event: StudyRoleDeletedEvent) {
        eventPublisher.publishEvent(
            StudyScheduleRoleDeletedEventDto(
                group = event.group.toEventDto(),
                schedule = event.schedule.toEventDto(),
                role = event.role.toEventDto()
            )
        )
    }

    private fun StudyScheduleGroupDto.toEventDto() = StudyScheduleEventGroupDto(
        id = identifier,
        ownerId = ownerId.toHexString(),
        name = name,
        profileImageThumbnail = profileImage.thumbnail
    )

    private fun StudySchedule.toEventDto() = StudyScheduleEventScheduleDto(
        id = identifier,
        groupId = groupId.toHexString(),
        session = session
    )

    private fun ScheduleRole.toEventDto() = StudyScheduleEventRoleDto(
        memberId = memberId.toHexString(),
        roles = roles
    )
}
