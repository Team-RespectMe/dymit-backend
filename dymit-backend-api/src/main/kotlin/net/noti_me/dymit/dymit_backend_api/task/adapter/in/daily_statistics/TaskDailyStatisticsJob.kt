package net.noti_me.dymit.dymit_backend_api.task.adapter.`in`.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.daily_statistics.CollectTaskDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.daily_statistics.CollectTaskDailyStatisticsUseCase
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

/**
 * Quartz entry point for task daily-statistics collection.
 */
@Component
@DisallowConcurrentExecution
class TaskDailyStatisticsJob(
    private val useCase: CollectTaskDailyStatisticsUseCase
) : Job {

    /**
     * Calculates the Korean daily window and invokes task collection.
     */
    override fun execute(context: JobExecutionContext?) {
        val window = DailyStatisticsWindowCalculator.calculate()
        useCase.execute(
            CollectTaskDailyStatisticsCommand(
                statisticDate = window.statisticDate,
                windowStart = window.windowStart,
                windowEnd = window.windowEnd
            )
        )
    }
}
