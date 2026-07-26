package net.noti_me.dymit.dymit_backend_api.admin.application.usecase

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.DailyMemberStatusDto
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.GetDailyMemberStatusCommand

/**
 * 기간별 일일 회원 현황을 조회하는 유스케이스입니다.
 */
interface GetDailyMemberStatusUseCase {

    /**
     * 지정한 기간의 일일 회원 현황을 조회합니다.
     *
     * @param command 조회 기간 명령
     * @return 일별 회원 현황 목록
     */
    fun execute(command: GetDailyMemberStatusCommand): List<DailyMemberStatusDto>
}
