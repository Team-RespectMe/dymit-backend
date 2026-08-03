package net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.daily_statistics

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Carries the business date and collection window for member daily statistics.
 */
data class CollectMemberDailyStatisticsCommand(
    val statisticDate: LocalDate,
    val windowStart: LocalDateTime,
    val windowEnd: LocalDateTime
)
