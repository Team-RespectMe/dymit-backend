package net.noti_me.dymit.dymit_backend_api.application.admin.dto

import java.time.LocalDateTime

class DailyMemberStatusDto(
    val newMemberCount: Long,
    val activeMemberCount: Long,
    val leaveMemberCount: Long,
    val totalMemberCount: Long,
    val recordedAt: LocalDateTime
) {
}