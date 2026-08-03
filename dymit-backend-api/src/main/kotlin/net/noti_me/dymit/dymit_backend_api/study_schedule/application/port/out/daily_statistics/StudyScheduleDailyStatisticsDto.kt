package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics

/**
 * Contains study-schedule-owned daily statistics values.
 */
data class StudyScheduleDailyStatisticsDto(
    val createdCount: Long,
    val participantMemberCount: Long
)
