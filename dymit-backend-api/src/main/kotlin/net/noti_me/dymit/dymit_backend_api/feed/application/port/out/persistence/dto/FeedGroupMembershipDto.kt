package net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto

/**
 * Feed 모듈에서 사용하는 그룹 가입 조회 결과입니다.
 *
 * @param groupIds 가입한 그룹 식별자 목록
 */
data class FeedGroupMembershipDto(
    val groupIds: List<String>
)
