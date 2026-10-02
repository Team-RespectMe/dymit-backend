package net.noti_me.dymit.dymit_backend_api.calendar.application.usecase

import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarDayPresenceDto
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarWeeklyPresenceCommand

/**
 * 로그인 회원의 주간 캘린더 존재 여부를 조회합니다.
 */
interface GetCalendarWeeklyPresenceUseCase {

    /**
     * 기준일이 속한 일요일부터 7일간의 캘린더를 조회합니다.
     *
     * @param command 주간 조회 명령
     * @return 날짜순 7일의 캘린더 목록
     */
    fun execute(command: GetCalendarWeeklyPresenceCommand): List<CalendarDayPresenceDto>
}
