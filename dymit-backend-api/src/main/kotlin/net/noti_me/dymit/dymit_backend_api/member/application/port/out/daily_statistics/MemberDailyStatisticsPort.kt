package net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics

import java.time.LocalDate
import java.time.Instant

/**
 * Provides member-owned metric collection and atomic daily-statistics persistence.
 */
interface MemberDailyStatisticsPort {

    /**
     * Counts member metrics in the inclusive/exclusive window.
     */
    fun collect(windowStart: Instant, windowEnd: Instant): MemberDailyStatisticsDto

    /**
     * Atomically upserts only the member section and returns whether this call inserted the document.
     */
    fun upsert(
        statisticDate: LocalDate,
        windowStart: Instant,
        windowEnd: Instant,
        statistics: MemberDailyStatisticsDto
    ): Boolean
}
