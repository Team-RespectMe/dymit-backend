package net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.dto

/**
 * 캘린더가 사용하는 스터디 그룹 조회 결과입니다.
 *
 * @property groupId 그룹 식별자
 * @property groupName 그룹 이름
 */
data class CalendarStudyGroupDto(
    val groupId: String,
    val groupName: String
)
