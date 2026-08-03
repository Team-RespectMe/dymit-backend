package net.noti_me.dymit.dymit_backend_api.common.daily_statistics

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents the business fields read from a completed daily-statistics document.
 */
data class DailyStatisticsReportDocument(
    val statisticDate: LocalDate,
    val windowStart: LocalDateTime,
    val windowEnd: LocalDateTime,
    val member: MemberDailyStatisticsReport,
    val studyGroup: StudyGroupDailyStatisticsReport,
    val studySchedule: StudyScheduleDailyStatisticsReport,
    val task: TaskDailyStatisticsReport,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Contains the member section of a daily-statistics report.
 */
data class MemberDailyStatisticsReport(
    val joinedCount: Long,
    val withdrawnCount: Long,
    val visitorCount: Long
)

/**
 * Contains the study-group section of a daily-statistics report.
 */
data class StudyGroupDailyStatisticsReport(
    val createdCount: Long
)

/**
 * Contains the study-schedule section of a daily-statistics report.
 */
data class StudyScheduleDailyStatisticsReport(
    val createdCount: Long,
    val participantMemberCount: Long
)

/**
 * Contains the task section of a daily-statistics report.
 */
data class TaskDailyStatisticsReport(
    val createdCount: Long,
    val submittedCount: Long
)
