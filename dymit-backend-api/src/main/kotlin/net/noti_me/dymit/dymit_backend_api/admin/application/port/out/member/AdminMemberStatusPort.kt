package net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member.dto.AdminMemberStatusDto
import java.time.Instant

/**
 * 관리자 모듈이 일별 회원 현황을 조회하는 출력 포트입니다.
 */
interface AdminMemberStatusPort {

    /**
     * 생성 시각 범위에 해당하는 회원 현황 기록을 조회합니다.
     *
     * @param start 조회 시작 시각
     * @param end 조회 종료 시각
     * @return 관리자 소유 회원 현황 DTO 목록
     */
    fun findAllByCreatedAtBetween(
        start: Instant,
        end: Instant
    ): List<AdminMemberStatusDto>
}
