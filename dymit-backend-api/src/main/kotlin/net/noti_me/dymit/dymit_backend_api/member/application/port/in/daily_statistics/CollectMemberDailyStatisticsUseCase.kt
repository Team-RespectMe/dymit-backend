package net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.daily_statistics

/**
 * Collects and stores member statistics for one daily window.
 */
interface CollectMemberDailyStatisticsUseCase {

    /**
     * Collects member metrics and atomically updates the member section.
     */
    fun execute(command: CollectMemberDailyStatisticsCommand)
}
