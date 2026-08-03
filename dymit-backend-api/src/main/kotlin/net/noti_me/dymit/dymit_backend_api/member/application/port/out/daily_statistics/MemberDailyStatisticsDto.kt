package net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics

/**
 * Contains member-owned daily statistics values.
 */
data class MemberDailyStatisticsDto(
    val joinedCount: Long,
    val withdrawnCount: Long,
    val visitorCount: Long
)
