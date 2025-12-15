package net.noti_me.dymit.dymit_backend_api.ports.persistence.member

import net.noti_me.dymit.dymit_backend_api.domain.member.DailyMemberStatus
import java.time.LocalDateTime

interface DailyMemberStatusRepository {

    fun save(status: DailyMemberStatus): DailyMemberStatus?

    fun findAllByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<DailyMemberStatus>
}