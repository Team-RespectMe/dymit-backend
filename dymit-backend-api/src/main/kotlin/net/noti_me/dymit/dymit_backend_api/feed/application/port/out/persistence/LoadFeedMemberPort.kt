package net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto.FeedMemberDto

/**
 * Feed가 필요한 최소 회원 정보를 조회하는 경계입니다.
 */
interface LoadFeedMemberPort {

    /**
     * 회원 정보를 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return Feed 소유 회원 데이터 또는 null
     */
    fun loadById(memberId: String): FeedMemberDto?
}
