package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.daily_statistics

/**
 * Collects and stores study-schedule statistics for one daily window.
 */
interface CollectStudyScheduleDailyStatisticsUseCase {

    /**
     * Collects schedule metrics and atomically updates the study-schedule section.
     */
    fun execute(command: CollectStudyScheduleDailyStatisticsCommand)
}
