package net.noti_me.dymit.dymit_backend_api.calendar.application

import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.GetCalendarDailyEventsUseCase
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarEventDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarDailyEventsCommand
import org.springframework.stereotype.Service

/**
 * 일별 캘린더 이벤트 조회 유즈케이스 구현입니다.
 *
 * @property eventQueryService 캘린더 이벤트 조합 서비스
 */
@Service
class GetCalendarDailyEventsService(
    private val eventQueryService: CalendarEventQueryService
) : GetCalendarDailyEventsUseCase {

    /**
     * 지정한 서울 날짜의 이벤트를 평탄한 목록으로 반환합니다.
     *
     * @param command 일별 조회 명령
     * @return 그룹 식별자와 이벤트 시각순 이벤트 목록
     */
    override fun execute(command: GetCalendarDailyEventsCommand): List<CalendarEventDto> {
        val period = CalendarPeriod.daily(command.date)
        return eventQueryService.loadEvents(command.memberId, period)
    }
}
