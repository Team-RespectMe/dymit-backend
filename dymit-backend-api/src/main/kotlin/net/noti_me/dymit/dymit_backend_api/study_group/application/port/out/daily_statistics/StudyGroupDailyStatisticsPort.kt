package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`out`.daily_statistics

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Provides study-group-owned metric collection and atomic daily-statistics persistence.
 */
interface StudyGroupDailyStatisticsPort {

    /**
     * Counts study groups created in the inclusive/exclusive window.
     */
    fun collect(windowStart: LocalDateTime, windowEnd: LocalDateTime): StudyGroupDailyStatisticsDto

    /**
     * Atomically upserts only the study-group section and returns whether this call inserted the document.
     */
    fun upsert(
        statisticDate: LocalDate,
        windowStart: LocalDateTime,
        windowEnd: LocalDateTime,
        statistics: StudyGroupDailyStatisticsDto
    ): Boolean
}
