package net.noti_me.dymit.dymit_backend_api.calendar.application

import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.LoadCalendarStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_group.dto.CalendarStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.LoadCalendarStudySchedulePort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.study_schedule.dto.CalendarStudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.LoadCalendarTaskPort
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.out.task.dto.CalendarTaskDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarEventDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarEventGroupDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarQueryDateParser
import net.noti_me.dymit.dymit_backend_api.calendar.domain.CalendarEventType
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate

/**
 * 캘린더 출력 포트의 회원별 일정과 과제를 캘린더 이벤트로 조합합니다.
 *
 * @property loadStudyGroupPort 캘린더 그룹 조회 포트
 * @property loadStudySchedulePort 캘린더 일정 조회 포트
 * @property loadTaskPort 캘린더 과제 조회 포트
 */
@Service
class CalendarEventQueryService(
    private val loadStudyGroupPort: LoadCalendarStudyGroupPort,
    private val loadStudySchedulePort: LoadCalendarStudySchedulePort,
    private val loadTaskPort: LoadCalendarTaskPort
) {

    /**
     * 회원의 참가 일정과 제출 대상 과제가 존재하는 서울 날짜를 조회합니다.
     *
     * @param memberId 회원 식별자
     * @param period 조회 기간
     * @return 이벤트가 존재하는 날짜 집합
     */
    fun loadEventDates(memberId: String, period: CalendarPeriod): Set<LocalDate> {
        val source = loadSourceEvents(memberId, period)
        return sequenceOf(
            source.schedules.asSequence().map { it.eventAt },
            source.tasks.asSequence().map { it.eventAt }
        ).flatten()
            .map { it.atZone(CalendarQueryDateParser.KOREA_ZONE).toLocalDate() }
            .toSet()
    }

    /**
     * 회원의 참가 일정과 제출 대상 과제를 일별 응답 정렬로 조회합니다.
     *
     * @param memberId 회원 식별자
     * @param period 조회 기간
     * @return 그룹 오름차순과 이벤트 시각 내림차순으로 정렬된 이벤트 목록
     */
    fun loadEvents(memberId: String, period: CalendarPeriod): List<CalendarEventDto> {
        val source = loadSourceEvents(memberId, period)
        val events = buildList {
            addAll(source.schedules.map { schedule -> schedule.toEvent(source.groupsById) })
            addAll(source.tasks.map { task -> task.toEvent(source.groupsById) })
        }

        return events.sortedWith(
            compareBy<CalendarEventDto> { it.group.id }
                .thenByDescending { it.eventAt }
                .thenBy { it.id }
                .thenBy { it.type.name }
        )
    }

    private fun loadSourceEvents(
        memberId: String,
        period: CalendarPeriod
    ): CalendarSourceEvents {
        val groupsById = loadStudyGroupPort.loadActiveGroupsByMemberId(memberId)
            .associateBy { it.groupId }
        if (groupsById.isEmpty()) {
            return CalendarSourceEvents(emptyMap(), emptyList(), emptyList())
        }

        val groupIds = groupsById.keys.toList()
        val schedules = loadStudySchedulePort.loadActiveSchedules(
            memberId = memberId,
            groupIds = groupIds,
            startInclusive = period.startInclusive,
            endExclusive = period.endExclusive
        ).filter { schedule ->
            schedule.groupId in groupsById && period.contains(schedule.eventAt)
        }
        val tasks = loadTaskPort.loadActiveTasks(
            memberId = memberId,
            groupIds = groupIds,
            startInclusive = period.startInclusive,
            endExclusive = period.endExclusive
        ).filter { task ->
            task.groupId in groupsById && period.contains(task.eventAt)
        }

        return CalendarSourceEvents(groupsById, schedules, tasks)
    }

    private fun CalendarPeriod.contains(eventAt: Instant): Boolean {
        return !eventAt.isBefore(startInclusive) && eventAt.isBefore(endExclusive)
    }

    private fun CalendarStudyScheduleDto.toEvent(
        groupsById: Map<String, CalendarStudyGroupDto>
    ): CalendarEventDto {
        return CalendarEventDto(
            type = CalendarEventType.STUDY_SCHEDULE,
            id = id,
            title = title,
            eventAt = eventAt,
            group = groupsById.getValue(groupId).toEventGroup()
        )
    }

    private fun CalendarTaskDto.toEvent(
        groupsById: Map<String, CalendarStudyGroupDto>
    ): CalendarEventDto {
        return CalendarEventDto(
            type = CalendarEventType.TASK,
            id = id,
            title = title,
            eventAt = eventAt,
            group = groupsById.getValue(groupId).toEventGroup()
        )
    }

    private fun CalendarStudyGroupDto.toEventGroup(): CalendarEventGroupDto {
        return CalendarEventGroupDto(id = groupId, name = groupName)
    }
}

/**
 * 포트에서 조회한 캘린더 원천 이벤트 묶음입니다.
 *
 * @property groupsById 활성 그룹 조회 결과
 * @property schedules 회원 참가 일정
 * @property tasks 회원 제출 대상 과제
 */
private data class CalendarSourceEvents(
    val groupsById: Map<String, CalendarStudyGroupDto>,
    val schedules: List<CalendarStudyScheduleDto>,
    val tasks: List<CalendarTaskDto>
)
