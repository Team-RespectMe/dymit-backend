package net.noti_me.dymit.dymit_backend_api.study_group.adapter.`in`.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.daily_statistics.CollectStudyGroupDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.daily_statistics.CollectStudyGroupDailyStatisticsUseCase
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

/**
 * Quartz entry point for study-group daily-statistics collection.
 */
@Component
@DisallowConcurrentExecution
class StudyGroupDailyStatisticsJob(
    private val useCase: CollectStudyGroupDailyStatisticsUseCase
) : Job {

    /**
     * Calculates the Korean daily window and invokes study-group collection.
     */
    override fun execute(context: JobExecutionContext?) {
        val window = DailyStatisticsWindowCalculator.calculate()
        useCase.execute(
            CollectStudyGroupDailyStatisticsCommand(
                statisticDate = window.statisticDate,
                windowStart = window.windowStart,
                windowEnd = window.windowEnd
            )
        )
    }
}
