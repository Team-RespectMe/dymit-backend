package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.daily_statistics

import java.time.LocalDate
import java.time.Instant

/**
 * Carries the business date and collection window for task daily statistics.
 */
data class CollectTaskDailyStatisticsCommand(
    val statisticDate: LocalDate,
    val windowStart: Instant,
    val windowEnd: Instant
)
