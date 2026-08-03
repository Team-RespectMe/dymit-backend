package net.noti_me.dymit.dymit_backend_api.member.adapter.`in`.daily_statistics

import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.daily_statistics.CollectMemberDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.daily_statistics.CollectMemberDailyStatisticsUseCase
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

/**
 * Quartz entry point for member daily-statistics collection.
 */
@Component
@DisallowConcurrentExecution
class MemberDailyStatisticsJob(
    private val useCase: CollectMemberDailyStatisticsUseCase
) : Job {

    /**
     * Calculates the Korean daily window and invokes member collection.
     */
    override fun execute(context: JobExecutionContext?) {
        val window = DailyStatisticsWindowCalculator.calculate()
        useCase.execute(
            CollectMemberDailyStatisticsCommand(
                statisticDate = window.statisticDate,
                windowStart = window.windowStart,
                windowEnd = window.windowEnd
            )
        )
    }
}
