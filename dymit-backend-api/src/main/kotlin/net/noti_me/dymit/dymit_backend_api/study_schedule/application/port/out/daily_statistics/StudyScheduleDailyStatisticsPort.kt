package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Provides schedule-owned metric collection and atomic daily-statistics persistence.
 */
interface StudyScheduleDailyStatisticsPort {

    /**
     * Counts schedules and distinct newly participating members in the inclusive/exclusive window.
     */
    fun collect(windowStart: LocalDateTime, windowEnd: LocalDateTime): StudyScheduleDailyStatisticsDto

    /**
     * Atomically upserts only the study-schedule section and returns whether this call inserted the document.
     */
    fun upsert(
        statisticDate: LocalDate,
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime,
        statistics: StudyScheduleDailyStatisticsDto
    ): Boolean
}
