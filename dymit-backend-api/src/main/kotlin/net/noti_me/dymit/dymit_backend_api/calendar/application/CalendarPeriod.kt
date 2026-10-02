package net.noti_me.dymit.dymit_backend_api.calendar.application

import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.CalendarQueryDateParser
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * 캘린더 조회 날짜와 시각 반개구간입니다.
 *
 * @property startDate 시작 날짜
 * @property endDateExclusive 종료 날짜
 * @property startInclusive 시작 시각
 * @property endExclusive 종료 시각
 */
data class CalendarPeriod(
    val startDate: LocalDate,
    val endDateExclusive: LocalDate,
    val startInclusive: Instant,
    val endExclusive: Instant
) {
    companion object {

        /**
         * 기준일이 속한 일요일부터 다음 일요일까지의 기간을 만듭니다.
         *
         * @param date 기준일
         * @return 주간 조회 기간
         */
        fun weekly(date: LocalDate): CalendarPeriod {
            return try {
                val startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                create(startDate, startDate.plusDays(WEEK_LENGTH))
            } catch (exception: DateTimeException) {
                throw invalidRange()
            } catch (exception: ArithmeticException) {
                throw invalidRange()
            }
        }

        /**
         * 지정한 날짜 하루의 기간을 만듭니다.
         *
         * @param date 조회일
         * @return 일별 조회 기간
         */
        fun daily(date: LocalDate): CalendarPeriod {
            return try {
                create(date, date.plusDays(1))
            } catch (exception: DateTimeException) {
                throw invalidRange()
            } catch (exception: ArithmeticException) {
                throw invalidRange()
            }
        }

        private fun create(startDate: LocalDate, endDateExclusive: LocalDate): CalendarPeriod {
            val startInclusive = startDate
                .atStartOfDay(CalendarQueryDateParser.KOREA_ZONE)
                .toInstant()
            val endExclusive = endDateExclusive
                .atStartOfDay(CalendarQueryDateParser.KOREA_ZONE)
                .toInstant()
            startInclusive.toEpochMilli()
            endExclusive.toEpochMilli()

            return CalendarPeriod(
                startDate = startDate,
                endDateExclusive = endDateExclusive,
                startInclusive = startInclusive,
                endExclusive = endExclusive
            )
        }

        private fun invalidRange(): BadRequestException {
            return BadRequestException(message = "date로 조회 기간을 계산할 수 없습니다.")
        }

        private const val WEEK_LENGTH = 7L
    }
}
