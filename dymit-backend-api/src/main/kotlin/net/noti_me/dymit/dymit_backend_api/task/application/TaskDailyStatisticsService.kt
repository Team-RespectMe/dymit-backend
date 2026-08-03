package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.daily_statistics.CollectTaskDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.daily_statistics.CollectTaskDailyStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics.TaskDailyStatisticsPort
import org.springframework.stereotype.Service

/**
 * Collects task metrics and stores them in the shared daily-statistics document.
 */
@Service
class TaskDailyStatisticsService(
    private val port: TaskDailyStatisticsPort
) : CollectTaskDailyStatisticsUseCase {

    /**
     * Collects and atomically persists the task-owned statistics fields.
     */
    override fun execute(command: CollectTaskDailyStatisticsCommand) {
        val statistics = port.collect(command.windowStart, command.windowEnd)
        port.upsert(
            statisticDate = command.statisticDate,
            windowStart = command.windowStart,
            windowEnd = command.windowEnd,
            statistics = statistics
        )
    }
}
