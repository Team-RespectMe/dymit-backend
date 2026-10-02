package net.noti_me.dymit.dymit_backend_api.calendar.application.usecase

import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarEventDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarDailyEventsCommand

/**
 * 로그인 회원의 일별 캘린더 이벤트를 조회합니다.
 */
interface GetCalendarDailyEventsUseCase {

    /**
     * 지정한 서울 날짜의 이벤트를 평탄한 목록으로 조회합니다.
     *
     * @param command 일별 조회 명령
     * @return 정렬된 이벤트 목록
     */
    fun execute(command: GetCalendarDailyEventsCommand): List<CalendarEventDto>
}
