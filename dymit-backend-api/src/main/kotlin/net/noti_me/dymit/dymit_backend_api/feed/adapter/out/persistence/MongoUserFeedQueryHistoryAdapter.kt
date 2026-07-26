package net.noti_me.dymit.dymit_backend_api.feed.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedQueryHistoryPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeedQueryHistory
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * MongoDB를 사용하는 피드 조회 이력 출력 어댑터입니다.
 *
 * @param mongoTemplate MongoDB 접근 객체
 */
@Repository
class MongoUserFeedQueryHistoryAdapter(
    private val mongoTemplate: MongoTemplate
) : UserFeedQueryHistoryPersistencePort {

    /**
     * 조회 이력을 저장합니다.
     *
     * @param history 저장할 조회 이력
     * @return 저장된 조회 이력
     */
    override fun save(history: UserFeedQueryHistory): UserFeedQueryHistory {
        return mongoTemplate.save(history)
    }

    /**
     * 회원의 조회 이력을 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 조회 이력 또는 null
     */
    override fun findByMemberId(memberId: ObjectId): UserFeedQueryHistory? {
        val query = Query()
            .addCriteria(Criteria.where("memberId").`is`(memberId))
        return mongoTemplate.findOne(query, UserFeedQueryHistory::class.java)
    }
}
