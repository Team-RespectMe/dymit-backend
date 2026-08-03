package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.daily_statistics

/**
 * Collects and stores task statistics for one daily window.
 */
interface CollectTaskDailyStatisticsUseCase {

    /**
     * Collects task metrics and atomically updates the task section.
     */
    fun execute(command: CollectTaskDailyStatisticsCommand)
}
