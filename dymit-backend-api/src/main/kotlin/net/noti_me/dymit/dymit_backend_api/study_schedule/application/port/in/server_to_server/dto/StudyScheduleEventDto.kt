package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto

import java.time.Instant

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

/**
 * 일정 수정 시 발행되는 이벤트입니다.
 *
 * @property group 변경된 일정이 속한 그룹
 * @property schedule 수정된 일정 정보
 * @property scheduleAt 변경된 일정 시작 시각
 * @property memberIds 일정 변경 알림을 받을 멤버 ID 목록
 */
data class StudyScheduleModifiedEventDto(
    val group: StudyScheduleEventGroupDto,
    val schedule: StudyScheduleEventScheduleDto,
    val scheduleAt: Instant,
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
