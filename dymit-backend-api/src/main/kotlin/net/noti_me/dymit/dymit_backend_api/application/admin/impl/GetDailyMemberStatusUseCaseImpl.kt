package net.noti_me.dymit.dymit_backend_api.application.admin.impl

import net.noti_me.dymit.dymit_backend_api.application.admin.dto.DailyMemberStatusDto
import net.noti_me.dymit.dymit_backend_api.application.admin.usecases.GetDailyMemberStatusUseCase
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.DailyMemberStatusRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GetDailyMemberStatusUseCaseImpl(
    private val dailyMemberStatusRepository: DailyMemberStatusRepository
): GetDailyMemberStatusUseCase {

    override fun getStatusBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<DailyMemberStatusDto> {
        // utc0 로 변환
        val startUtc0 = startDate.minusHours(9)
        val endUtc0 = endDate.minusHours(9)

        val items = dailyMemberStatusRepository.findAllByCreatedAtBetween(
            startUtc0,
            endUtc0
        )

        return items.map {
            DailyMemberStatusDto(
                newMemberCount = it.newMemberCount,
                activeMemberCount = it.activeMemberCount,
                leaveMemberCount = it.leaveMemberCount,
                totalMemberCount = it.totalMemberCount,
                recordedAt = it.createdAt
            )
        }
    }
}