package net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.calendar.domain.CalendarEventType
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import java.time.Instant
import java.time.LocalDate

/**
 * 캘린더 이벤트의 그룹 DTO입니다.
 *
 * @property id 그룹 식별자
 * @property name 그룹 이름
 */
data class CalendarEventGroupDto(
    val id: String,
    val name: String
)

/**
 * 캘린더 이벤트 DTO입니다.
 *
 * @property type 이벤트 유형
 * @property id 이벤트 식별자
 * @property title 이벤트 제목
 * @property eventAt 이벤트 발생 시각
 * @property group 이벤트가 속한 그룹
 * @property submissionType 과제 제출 유형
 * @property location 일정 장소
 * @property participantCount 일정의 활성 참가 등록 회원 수
 * @property isParticipant 조회 회원의 활성 참가 등록 여부
 */
data class CalendarEventDto(
    val type: CalendarEventType,
    val id: String,
    val title: String,
    val eventAt: Instant,
    val group: CalendarEventGroupDto,
    val submissionType: TaskSubmissionType? = null,
    val location: LocationVo? = null,
    val participantCount: Long? = null,
    val isParticipant: Boolean? = null
)

/**
 * 날짜별 주간 캘린더 DTO입니다.
 *
 * @property date 날짜
 * @property hasEvents 이벤트 존재 여부
 */
data class CalendarDayPresenceDto(
    val date: LocalDate,
    val hasEvents: Boolean
)
