package net.noti_me.dymit.dymit_backend_api.calendar.adapter.`in`.web

import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.`in`.web.CalendarApi
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.`in`.web.dto.CalendarDayPresenceItem
import net.noti_me.dymit.dymit_backend_api.calendar.application.port.`in`.web.dto.CalendarEventItem
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.GetCalendarDailyEventsUseCase
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.GetCalendarWeeklyPresenceUseCase
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarDailyEventsCommand
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.GetCalendarWeeklyPresenceCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 로그인 회원 캘린더 조회 요청을 처리하는 컨트롤러입니다.
 *
 * @property getWeeklyPresenceUseCase 주간 조회 유즈케이스
 * @property getDailyEventsUseCase 일별 조회 유즈케이스
 */
@RestController
@RequestMapping("/api/v1/members/me/calendar")
class CalendarController(
    private val getWeeklyPresenceUseCase: GetCalendarWeeklyPresenceUseCase,
    private val getDailyEventsUseCase: GetCalendarDailyEventsUseCase
) : CalendarApi {

    /**
     * 주간 캘린더 조회 요청을 명령으로 변환해 처리합니다.
     */
    @GetMapping("/weekly-presence")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getWeeklyPresence(
        @LoginMember memberInfo: MemberInfo,
        @RequestParam(required = false) date: String?
    ): ListResponse<CalendarDayPresenceItem> {
        val command = GetCalendarWeeklyPresenceCommand.from(memberInfo.memberId, date)
        return ListResponse.from(
            getWeeklyPresenceUseCase.execute(command).map(CalendarDayPresenceItem::from)
        )
    }

    /**
     * 일별 캘린더 조회 요청을 명령으로 변환해 처리합니다.
     */
    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getDailyEvents(
        @LoginMember memberInfo: MemberInfo,
        @RequestParam(required = false) date: String?
    ): ListResponse<CalendarEventItem> {
        val command = GetCalendarDailyEventsCommand.from(memberInfo.memberId, date)
        return ListResponse.from(
            getDailyEventsUseCase.execute(command).map(CalendarEventItem::from)
        )
    }
}
