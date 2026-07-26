package net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeedQueryHistory
import org.bson.types.ObjectId

/**
 * 개인 피드 조회 이력 영속성 경계입니다.
 */
interface UserFeedQueryHistoryPersistencePort {

    /**
     * 조회 이력을 저장합니다.
     *
     * @param history 저장할 조회 이력
     * @return 저장된 조회 이력
     */
    fun save(history: UserFeedQueryHistory): UserFeedQueryHistory

    /**
     * 회원의 조회 이력을 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 조회 이력 또는 null
     */
    fun findByMemberId(memberId: ObjectId): UserFeedQueryHistory?
}
