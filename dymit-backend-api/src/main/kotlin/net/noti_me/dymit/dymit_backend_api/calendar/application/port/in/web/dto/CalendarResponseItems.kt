package net.noti_me.dymit.dymit_backend_api.calendar.application.port.`in`.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarDayPresenceDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarEventDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarEventGroupDto
import net.noti_me.dymit.dymit_backend_api.calendar.domain.CalendarEventType
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import java.time.Instant
import java.time.LocalDate

/**
 * 캘린더 이벤트의 그룹 응답 항목입니다.
 *
 * @property id 그룹 식별자
 * @property name 그룹 이름
 */
@Schema(description = "이벤트가 속한 그룹")
data class CalendarEventGroupItem(
    @field:Schema(description = "그룹 식별자", example = "66fdb34705af234c41b32d01")
    val id: String,
    @field:Schema(description = "그룹 이름", example = "알고리즘 스터디")
    val name: String
) {
    companion object {

        /**
         * 애플리케이션 그룹 DTO를 응답 항목으로 변환합니다.
         *
         * @param dto 이벤트 그룹 DTO
         * @return 이벤트 그룹 응답 항목
         */
        fun from(dto: CalendarEventGroupDto): CalendarEventGroupItem {
            return CalendarEventGroupItem(id = dto.id, name = dto.name)
        }
    }
}

/** 일별 캘린더 스터디 일정의 장소 응답 항목입니다. */
@Schema(description = "스터디 일정 장소 정보")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CalendarEventLocationItem(
    @field:Schema(description = "장소 유형", example = "ONLINE")
    val type: ScheduleLocation.LocationType,
    @field:Schema(description = "장소 이름 또는 접속 정보", example = "화상 회의")
    val value: String,
    @field:Schema(description = "온라인 회의 URL", example = "https://example.com/meeting", nullable = true)
    val link: String?
) {
    companion object {
        fun from(location: LocationVo): CalendarEventLocationItem {
            return CalendarEventLocationItem(
                type = location.type,
                value = location.value,
                link = location.link
            )
        }
    }
}

/**
 * 일별 캘린더 이벤트 응답 항목입니다.
 *
 * @property type 이벤트 유형
 * @property id 이벤트 식별자
 * @property title 이벤트 제목
 * @property eventAt 이벤트 발생 시각
 * @property group 이벤트가 속한 그룹
 * @property submissionType 과제 제출 유형 (TASK일 때만 포함)
 * @property location 장소 정보 (STUDY_SCHEDULE일 때만 포함)
 */
@Schema(description = "일별 캘린더 이벤트")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CalendarEventItem(
    @field:Schema(description = "이벤트 유형", example = "TASK")
    val type: CalendarEventType,
    @field:Schema(description = "이벤트 식별자", example = "66fdb34705af234c41b32d10")
    val id: String,
    @field:Schema(description = "이벤트 제목", example = "주간 과제")
    val title: String,
    @field:Schema(description = "이벤트 시각", example = "2026-10-02T10:00:00Z")
    val eventAt: Instant,
    @field:Schema(description = "이벤트가 속한 그룹")
    val group: CalendarEventGroupItem,
    @field:Schema(description = "과제 제출 유형. TASK 이벤트에만 포함됩니다.", example = "OUTPUT", nullable = true)
    val submissionType: TaskSubmissionType? = null,
    @field:Schema(description = "장소 정보. STUDY_SCHEDULE 이벤트에만 포함됩니다.", nullable = true)
    val location: CalendarEventLocationItem? = null
) {
    companion object {

        /**
         * 애플리케이션 DTO를 이벤트 응답 항목으로 변환합니다.
         *
         * @param dto 이벤트 DTO
         * @return 이벤트 응답 항목
         */
        fun from(dto: CalendarEventDto): CalendarEventItem {
            return CalendarEventItem(
                type = dto.type,
                id = dto.id,
                title = dto.title,
                eventAt = dto.eventAt,
                group = CalendarEventGroupItem.from(dto.group),
                submissionType = dto.submissionType,
                location = dto.location?.let(CalendarEventLocationItem::from)
            )
        }
    }
}

/**
 * 날짜별 주간 캘린더 존재 여부 응답 항목입니다.
 *
 * @property date 날짜
 * @property hasEvents 이벤트 존재 여부
 */
@Schema(description = "날짜별 주간 캘린더 이벤트 존재 여부")
data class CalendarDayPresenceItem(
    @field:Schema(description = "서울 기준 날짜", example = "2026-10-02")
    val date: LocalDate,
    @field:Schema(description = "이벤트 존재 여부", example = "true")
    val hasEvents: Boolean
) {
    companion object {

        /**
         * 애플리케이션 DTO를 날짜별 응답 항목으로 변환합니다.
         *
         * @param dto 날짜별 캘린더 DTO
         * @return 날짜별 응답 항목
         */
        fun from(dto: CalendarDayPresenceDto): CalendarDayPresenceItem {
            return CalendarDayPresenceItem(date = dto.date, hasEvents = dto.hasEvents)
        }
    }
}
