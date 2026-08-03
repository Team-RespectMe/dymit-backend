package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.daily_statistics.CollectStudyGroupDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.daily_statistics.CollectStudyGroupDailyStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`out`.daily_statistics.StudyGroupDailyStatisticsPort
import org.springframework.stereotype.Service

/**
 * Collects study-group metrics and stores them in the shared daily-statistics document.
 */
@Service
class StudyGroupDailyStatisticsService(
    private val port: StudyGroupDailyStatisticsPort
) : CollectStudyGroupDailyStatisticsUseCase {

    /**
     * Collects and atomically persists the study-group-owned statistics fields.
     */
    override fun execute(command: CollectStudyGroupDailyStatisticsCommand) {
        val statistics = port.collect(command.windowStart, command.windowEnd)
        port.upsert(
            statisticDate = command.statisticDate,
            windowStart = command.windowStart,
            windowEnd = command.windowEnd,
            statistics = statistics
        )
    }
}
