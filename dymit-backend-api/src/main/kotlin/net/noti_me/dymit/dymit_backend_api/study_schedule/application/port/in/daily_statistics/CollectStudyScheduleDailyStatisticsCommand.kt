package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.daily_statistics

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Carries the business date and collection window for study-schedule daily statistics.
 */
data class CollectStudyScheduleDailyStatisticsCommand(
    val statisticDate: LocalDate,
    val windowStart: LocalDateTime,
    val windowEnd: LocalDateTime
)
