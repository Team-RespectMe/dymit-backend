package net.noti_me.dymit.dymit_backend_api.common.daily_statistics

import java.time.LocalDate
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Represents the Korean business date and its inclusive/exclusive collection window.
 */
data class DailyStatisticsWindow(
    val statisticDate: LocalDate,
    val windowStart: Instant,
    val windowEnd: Instant
)

/**
 * Calculates the previous Korean calendar day's 04:00-to-04:00 statistics window.
 */
object DailyStatisticsWindowCalculator {

    val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    private val BOUNDARY_TIME: LocalTime = LocalTime.of(4, 0)

    /**
     * Calculates a deterministic window from the supplied instant without using the server time zone.
     */
    fun calculate(now: Instant = Instant.now()): DailyStatisticsWindow {
        val currentDate = now.atZone(KOREA_ZONE).toLocalDate()
        val statisticDate = currentDate.minusDays(1)
        return DailyStatisticsWindow(
            statisticDate = statisticDate,
            windowStart = statisticDate.atTime(BOUNDARY_TIME).atZone(KOREA_ZONE).toInstant(),
            windowEnd = currentDate.atTime(BOUNDARY_TIME).atZone(KOREA_ZONE).toInstant()
        )
    }
}
