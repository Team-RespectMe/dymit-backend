package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.dto.ServerNoticeMemberDto

/**
 * 서버 공지에서 필요한 멤버 정보를 조회하는 출력 포트입니다.
 */
interface ServerNoticeMemberPort {

    /**
     * 멤버 식별자로 공지 작성자 정보를 조회합니다.
     *
     * @param memberId 멤버 식별자
     * @return 서버 공지 전용 멤버 DTO, 없으면 null
     */
    fun loadById(memberId: String): ServerNoticeMemberDto?
}
