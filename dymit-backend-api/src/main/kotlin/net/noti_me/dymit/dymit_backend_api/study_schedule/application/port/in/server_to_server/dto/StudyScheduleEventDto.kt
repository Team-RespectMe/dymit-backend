package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto

data class StudyScheduleEventGroupDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val profileImageThumbnail: String
)

data class StudyScheduleEventScheduleDto(
    val id: String,
    val groupId: String,
    val session: Long
)

data class StudyScheduleEventMemberDto(
    val memberId: String,
    val nickname: String
)

data class StudyScheduleEventRoleDto(
    val memberId: String,
    val roles: List<String>
)

data class StudyScheduleCreatedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto
)

data class StudyScheduleModifiedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val memberIds: List<String>
)

data class StudyScheduleCanceledEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val memberIds: List<String>
)

data class StudyScheduleParticipatedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val member: StudyScheduleEventMemberDto
)

data class StudyScheduleParticipationCanceledEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val member: StudyScheduleEventMemberDto
)

data class StudyScheduleCommentCreatedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val commentId: String
)

data class StudyScheduleRoleAssignedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val role: StudyScheduleEventRoleDto
)

data class StudyScheduleRoleChangedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val role: StudyScheduleEventRoleDto
)

data class StudyScheduleRoleDeletedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val role: StudyScheduleEventRoleDto
)
