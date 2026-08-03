package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.daily_statistics

/**
 * Collects and stores study-group statistics for one daily window.
 */
interface CollectStudyGroupDailyStatisticsUseCase {

    /**
     * Collects study-group metrics and atomically updates the study-group section.
     */
    fun execute(command: CollectStudyGroupDailyStatisticsCommand)
}
