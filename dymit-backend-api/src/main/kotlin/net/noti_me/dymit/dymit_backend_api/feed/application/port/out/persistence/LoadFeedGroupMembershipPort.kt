package net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto.FeedGroupMembershipDto

/**
 * Feed가 필요한 회원의 그룹 가입 정보를 조회하는 경계입니다.
 */
interface LoadFeedGroupMembershipPort {

    /**
     * 회원이 가입한 그룹 식별자 목록을 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return Feed 소유 그룹 가입 데이터
     */
    fun loadByMemberId(memberId: String): FeedGroupMembershipDto
}
