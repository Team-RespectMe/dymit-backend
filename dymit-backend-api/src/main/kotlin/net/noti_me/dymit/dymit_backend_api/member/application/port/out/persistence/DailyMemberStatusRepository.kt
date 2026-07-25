package net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.member.domain.DailyMemberStatus
import java.time.LocalDateTime

interface DailyMemberStatusRepository {

    fun save(status: DailyMemberStatus): DailyMemberStatus?

    fun findAllByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<DailyMemberStatus>
}