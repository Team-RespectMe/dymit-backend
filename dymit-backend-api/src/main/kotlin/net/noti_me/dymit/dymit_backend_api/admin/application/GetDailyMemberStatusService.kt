package net.noti_me.dymit.dymit_backend_api.admin.application

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.DailyMemberStatusDto
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.GetDailyMemberStatusCommand
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member.AdminMemberStatusPort
import net.noti_me.dymit.dymit_backend_api.admin.application.usecase.GetDailyMemberStatusUseCase
import org.springframework.stereotype.Service

/**
 * 관리자용 일별 회원 현황 조회를 처리합니다.
 */
@Service
class GetDailyMemberStatusService(
    private val adminMemberStatusPort: AdminMemberStatusPort
) : GetDailyMemberStatusUseCase {

    /**
     * 한국 표준시 입력을 UTC 기준 조회 범위로 변환해 현황을 반환합니다.
     */
    override fun execute(command: GetDailyMemberStatusCommand): List<DailyMemberStatusDto> {
        val items = adminMemberStatusPort.findAllByCreatedAtBetween(
            start = command.startDate.minusHours(9),
            end = command.endDate.minusHours(9)
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
