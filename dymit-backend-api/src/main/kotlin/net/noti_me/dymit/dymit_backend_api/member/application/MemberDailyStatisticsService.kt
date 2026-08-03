package net.noti_me.dymit.dymit_backend_api.member.application

import net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.daily_statistics.CollectMemberDailyStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.daily_statistics.CollectMemberDailyStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics.MemberDailyStatisticsPort
import org.springframework.stereotype.Service

/**
 * Collects member metrics and stores them in the shared daily-statistics document.
 */
@Service
class MemberDailyStatisticsService(
    private val port: MemberDailyStatisticsPort
) : CollectMemberDailyStatisticsUseCase {

    /**
     * Collects and atomically persists the member-owned statistics fields.
     */
    override fun execute(command: CollectMemberDailyStatisticsCommand) {
        val statistics = port.collect(command.windowStart, command.windowEnd)
        port.upsert(
            statisticDate = command.statisticDate,
            windowStart = command.windowStart,
            windowEnd = command.windowEnd,
            statistics = statistics
        )
    }
}
