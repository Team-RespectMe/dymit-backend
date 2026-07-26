package net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed

/**
 * 개인 피드 영속성 경계입니다.
 */
interface UserFeedPersistencePort {

    /**
     * 개인 피드 목록을 저장합니다.
     *
     * @param userFeeds 저장할 피드 목록
     */
    fun saveAll(userFeeds: List<UserFeed>)

    /**
     * 개인 피드를 저장합니다.
     *
     * @param userFeed 저장할 피드
     * @return 저장된 피드
     */
    fun save(userFeed: UserFeed): UserFeed

    /**
     * 식별자로 개인 피드를 조회합니다.
     *
     * @param id 피드 식별자
     * @return 조회된 피드 또는 null
     */
    fun findById(id: String): UserFeed?

    /**
     * 회원의 개인 피드를 최신순으로 조회합니다.
     *
     * @param memberId 회원 식별자
     * @param cursor 조회 커서
     * @param size 조회 개수
     * @return 개인 피드 목록
     */
    fun findByMemberIdOrderByCreatedAtDesc(
        memberId: String,
        cursor: String?,
        size: Long
    ): List<UserFeed>

    /**
     * 식별자로 개인 피드를 삭제합니다.
     *
     * @param id 피드 식별자
     * @return 삭제 여부
     */
    fun deleteById(id: String): Boolean
}
