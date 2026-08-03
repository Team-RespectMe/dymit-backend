package net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics

/**
 * Contains task-owned daily statistics values.
 */
data class TaskDailyStatisticsDto(
    val createdCount: Long,
    val submittedCount: Long
)
