package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoUserFeedRepository(
    private val mongoTemplate: MongoTemplate
) : UserFeedRepository {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    override fun save(userFeed: UserFeed): UserFeed {
        return mongoTemplate.save(userFeed)
    }

    override fun findById(id: String): UserFeed? {
        return mongoTemplate.findById(ObjectId(id), UserFeed::class.java)
    }

    override fun findByMemberId(
        memberId: String,
        cursor: String?,
        size: Long
    ): List<UserFeed> {
        // 1. 특정 멤버의 피드만 조회 + 삭제되지 않은 데이터만
        val criteria = Criteria.where("memberId").`is`(ObjectId(memberId))

        // 3. cursor가 주어진 경우, 해당 cursor ID를 포함하여 그 이전(더 오래된) 피드들을 조회
        if (!cursor.isNullOrEmpty()) {
            val cursorObjectId = ObjectId(cursor)
            criteria.and("_id").lte(cursorObjectId)
        }

        // 2. 피드를 최신 순(DESC)으로 정렬하고 size만큼 제한
        val query = Query(criteria)
            .with(Sort.by(Sort.Direction.DESC, "_id"))
            .limit(size.toInt())

        val result = mongoTemplate.find(query, UserFeed::class.java)
        return result
    }

    override fun deleteById(id: String): Boolean {
        val query = Query(Criteria.where("_id").`is`(ObjectId(id)))
        val result = mongoTemplate.remove(query, UserFeed::class.java)
        return result.deletedCount > 0
    }
}
