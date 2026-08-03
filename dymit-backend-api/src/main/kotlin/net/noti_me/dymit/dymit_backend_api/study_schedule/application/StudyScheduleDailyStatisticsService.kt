package net.noti_me.dymit.dymit_backend_api.study_schedule.application

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.daily_statistics.CollectStudyScheduleDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.daily_statistics.CollectStudyScheduleDailyStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics.StudyScheduleDailyStatisticsPort
import org.springframework.stereotype.Service

/**
 * Collects study-schedule metrics and stores them in the shared daily-statistics document.
 */
@Service
class StudyScheduleDailyStatisticsService(
    private val port: StudyScheduleDailyStatisticsPort
) : CollectStudyScheduleDailyStatisticsUseCase {

    /**
     * Collects and atomically persists the study-schedule-owned statistics fields.
     */
    override fun execute(command: CollectStudyScheduleDailyStatisticsCommand) {
        val statistics = port.collect(command.windowStart, command.windowEnd)
        port.upsert(
            statisticDate = command.statisticDate,
            windowStart = command.windowStart,
            windowEnd = command.windowEnd,
            statistics = statistics
        )
    }
}
