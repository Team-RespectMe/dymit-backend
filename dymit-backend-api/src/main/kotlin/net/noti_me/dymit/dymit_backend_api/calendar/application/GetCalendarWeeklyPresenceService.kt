package net.noti_me.dymit.dymit_backend_api.calendar.application

import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.GetCalendarWeeklyPresenceUseCase
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarDayPresenceDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarWeeklyPresenceCommand
import org.springframework.stereotype.Service

/**
 * 주간 캘린더 존재 여부 조회 유즈케이스 구현입니다.
 *
 * @property eventQueryService 캘린더 이벤트 조합 서비스
 */
@Service
class GetCalendarWeeklyPresenceService(
    private val eventQueryService: CalendarEventQueryService
) : GetCalendarWeeklyPresenceUseCase {

    /**
     * 기준일이 속한 주의 일요일부터 7일을 날짜순으로 반환합니다.
     *
     * @param command 주간 조회 명령
     * @return 날짜순 7일의 캘린더 목록
     */
    override fun execute(command: GetCalendarWeeklyPresenceCommand): List<CalendarDayPresenceDto> {
        val period = CalendarPeriod.weekly(command.date)
        val eventDates = eventQueryService.loadEventDates(command.memberId, period)

        return (0 until WEEK_LENGTH).map { dayOffset ->
            val date = period.startDate.plusDays(dayOffset.toLong())
            CalendarDayPresenceDto(
                date = date,
                hasEvents = date in eventDates
            )
        }
    }

    companion object {
        private const val WEEK_LENGTH = 7
    }
}
