package net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.`in`.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.daily_statistics.CollectStudyScheduleDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.daily_statistics.CollectStudyScheduleDailyStatisticsUseCase
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

/**
 * Quartz entry point for study-schedule daily-statistics collection.
 */
@Component
@DisallowConcurrentExecution
class StudyScheduleDailyStatisticsJob(
    private val useCase: CollectStudyScheduleDailyStatisticsUseCase
) : Job {

    /**
     * Calculates the Korean daily window and invokes study-schedule collection.
     */
    override fun execute(context: JobExecutionContext?) {
        val window = DailyStatisticsWindowCalculator.calculate()
        useCase.execute(
            CollectStudyScheduleDailyStatisticsCommand(
                statisticDate = window.statisticDate,
                windowStart = window.windowStart,
                windowEnd = window.windowEnd
            )
        )
    }
}
