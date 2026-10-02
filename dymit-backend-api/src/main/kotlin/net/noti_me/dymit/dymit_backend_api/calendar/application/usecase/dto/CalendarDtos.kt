package net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.calendar.domain.CalendarEventType
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
 */
data class CalendarEventDto(
    val type: CalendarEventType,
    val id: String,
    val title: String,
    val eventAt: Instant,
    val group: CalendarEventGroupDto
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
