package net.noti_me.dymit.dymit_backend_api.feed.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

/**
 * MongoDB를 사용하는 개인 피드 출력 어댑터입니다.
 *
 * @param mongoTemplate MongoDB 접근 객체
 */
@Repository
class MongoUserFeedAdapter(
    private val mongoTemplate: MongoTemplate
) : UserFeedPersistencePort {

    /**
     * 개인 피드 목록을 기존 bulk upsert 방식으로 저장합니다.
     *
     * @param userFeeds 저장할 피드 목록
     */
    override fun saveAll(userFeeds: List<UserFeed>) {
        if (userFeeds.isEmpty()) return

        val bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, UserFeed::class.java)
        userFeeds.forEach { userFeed ->
            val id = userFeed.id
            if (id != null) {
                val query = Query(Criteria.where("_id").`is`(id))
                val update = Update()
                    .set("memberId", userFeed.memberId)
                    .set("iconType", userFeed.iconType)
                    .set("messages", userFeed.messages)
                    .set("associates", userFeed.associates)
                    .set("isRead", userFeed.isRead)
                    .set("createdAt", userFeed.createdAt)
                    .set("updatedAt", userFeed.updatedAt)
                    .set("isDeleted", userFeed.isDeleted)
                bulkOps.upsert(query, update)
            } else {
                bulkOps.insert(userFeed)
            }
        }
        bulkOps.execute()
    }

    /**
     * 개인 피드를 저장합니다.
     *
     * @param userFeed 저장할 피드
     * @return 저장된 피드
     */
    override fun save(userFeed: UserFeed): UserFeed {
        return mongoTemplate.save(userFeed)
    }

    /**
     * 식별자로 개인 피드를 조회합니다.
     *
     * @param id 피드 식별자
     * @return 개인 피드 또는 null
     */
    override fun findById(id: String): UserFeed? {
        return mongoTemplate.findById(ObjectId(id), UserFeed::class.java)
    }

    /**
     * 회원의 개인 피드를 기존 커서 조건과 최신순 정렬로 조회합니다.
     *
     * @param memberId 회원 식별자
     * @param cursor 조회 커서
     * @param size 조회 개수
     * @return 개인 피드 목록
     */
    override fun findByMemberIdOrderByCreatedAtDesc(
        memberId: String,
        cursor: String?,
        size: Long
    ): List<UserFeed> {
        val criteria = Criteria.where("memberId").`is`(ObjectId(memberId))
        if (!cursor.isNullOrEmpty()) {
            criteria.and("_id").lt(ObjectId(cursor))
        }

        val query = Query(criteria)
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(size.toInt())
        return mongoTemplate.find(query, UserFeed::class.java)
    }

    /**
     * 식별자로 개인 피드를 삭제합니다.
     *
     * @param id 피드 식별자
     * @return 삭제 여부
     */
    override fun deleteById(id: String): Boolean {
        val query = Query(Criteria.where("_id").`is`(ObjectId(id)))
        return mongoTemplate.remove(query, UserFeed::class.java).deletedCount > 0
    }
}
