package net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Provides task-owned metric collection and atomic daily-statistics persistence.
 */
interface TaskDailyStatisticsPort {

    /**
     * Counts task creations and submissions in the inclusive/exclusive window.
     */
    fun collect(windowStart: LocalDateTime, windowEnd: LocalDateTime): TaskDailyStatisticsDto

    /**
     * Atomically upserts only the task section and returns whether this call inserted the document.
     */
    fun upsert(
        statisticDate: LocalDate,
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime,
        statistics: TaskDailyStatisticsDto
    ): Boolean
}
