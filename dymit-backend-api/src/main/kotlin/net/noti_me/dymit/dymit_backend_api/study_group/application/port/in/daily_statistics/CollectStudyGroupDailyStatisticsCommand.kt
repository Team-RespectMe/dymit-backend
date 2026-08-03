package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.daily_statistics

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Carries the business date and collection window for study-group daily statistics.
 */
data class CollectStudyGroupDailyStatisticsCommand(
    val statisticDate: LocalDate,
    val windowStart: LocalDateTime,
    val windowEnd: LocalDateTime
)
