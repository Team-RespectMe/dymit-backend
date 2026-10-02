package net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId

/**
 * 주간 캘린더 존재 여부 조회 명령입니다.
 *
 * @property memberId 로그인 회원 식별자
 * @property date 조회 기준일
 */
data class GetCalendarWeeklyPresenceCommand(
    val memberId: String,
    val date: LocalDate
) {
    companion object {

        /**
         * 문자열 쿼리 값으로 주간 조회 명령을 만듭니다.
         *
         * @param memberId 로그인 회원 식별자
         * @param date ISO 날짜 문자열
         * @param today 날짜가 없을 때 사용할 서울 기준 현재 날짜
         * @return 주간 조회 명령
         */
        fun from(
            memberId: String,
            date: String?,
            today: LocalDate = LocalDate.now(CalendarQueryDateParser.KOREA_ZONE)
        ): GetCalendarWeeklyPresenceCommand {
            return GetCalendarWeeklyPresenceCommand(
                memberId = memberId,
                date = CalendarQueryDateParser.parse(date, today)
            )
        }
    }
}

/**
 * 일별 캘린더 이벤트 조회 명령입니다.
 *
 * @property memberId 로그인 회원 식별자
 * @property date 조회일
 */
data class GetCalendarDailyEventsCommand(
    val memberId: String,
    val date: LocalDate
) {
    companion object {

        /**
         * 문자열 쿼리 값으로 일별 조회 명령을 만듭니다.
         *
         * @param memberId 로그인 회원 식별자
         * @param date ISO 날짜 문자열
         * @param today 날짜가 없을 때 사용할 서울 기준 현재 날짜
         * @return 일별 조회 명령
         */
        fun from(
            memberId: String,
            date: String?,
            today: LocalDate = LocalDate.now(CalendarQueryDateParser.KOREA_ZONE)
        ): GetCalendarDailyEventsCommand {
            return GetCalendarDailyEventsCommand(
                memberId = memberId,
                date = CalendarQueryDateParser.parse(date, today)
            )
        }
    }
}

/**
 * 캘린더 날짜 쿼리를 해석합니다.
 */
object CalendarQueryDateParser {

    val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    /**
     * ISO 날짜 문자열을 해석하고 값이 없으면 전달받은 현재 날짜를 반환합니다.
     *
     * @param value ISO 날짜 문자열
     * @param today 기본 날짜
     * @return 해석된 날짜
     */
    fun parse(value: String?, today: LocalDate): LocalDate {
        if (value == null) {
            return today
        }

        return try {
            LocalDate.parse(value)
        } catch (exception: DateTimeException) {
            throw BadRequestException(message = "date는 ISO 날짜 형식이어야 합니다.")
        }
    }
}
