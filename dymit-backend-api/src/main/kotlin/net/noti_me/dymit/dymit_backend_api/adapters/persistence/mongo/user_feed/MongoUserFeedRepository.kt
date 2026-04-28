package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class MongoUserFeedRepository(
    private val mongoTemplate: MongoTemplate
) : UserFeedRepository {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    override fun saveAll(userFeeds: List<UserFeed>) {
        if (userFeeds.isEmpty()) return
        val bulkOps = mongoTemplate.bulkOps(org.springframework.data.mongodb.core.BulkOperations.BulkMode.UNORDERED, UserFeed::class.java)
        userFeeds.forEach { userFeed ->
            val id = userFeed.id
            if (id != null) {
                // upsert: id가 있으면 update, 없으면 insert
                val query = Query(Criteria.where("_id").`is`(id))
                val update = Update()
                // UserFeed의 모든 필드를 update에 set (여기서는 예시로, 실제로는 UserFeed의 필드에 맞게 추가 필요)
                update.set("memberId", userFeed.memberId)
                update.set("iconType", userFeed.iconType)
                update.set("messages", userFeed.messages)
                update.set("associates", userFeed.associates)
                update.set("isRead", userFeed.isRead)
                update.set("createdAt", userFeed.createdAt)
                update.set("updatedAt", userFeed.updatedAt)
                update.set("isDeleted", userFeed.isDeleted)
                bulkOps.upsert(query, update)
            } else {
                // id가 없으면 insert
                bulkOps.insert(userFeed)
            }
        }
        bulkOps.execute()
    }

    override fun save(userFeed: UserFeed): UserFeed {
        return mongoTemplate.save(userFeed)
    }

    override fun findById(id: String): UserFeed? {
        return mongoTemplate.findById(ObjectId(id), UserFeed::class.java)
    }

    override fun findByMemberIdOrderByCreatedAtDesc(
        memberId: String,
        cursor: String?,
        size: Long
    ): List<UserFeed> {
        // 1. 특정 멤버의 피드만 조회 + 삭제되지 않은 데이터만
        val criteria = Criteria.where("memberId").`is`(ObjectId(memberId))

        // 3. cursor가 주어진 경우, 해당 cursor ID를 포함하여 그 이전(더 오래된) 피드들을 조회
        if (!cursor.isNullOrEmpty()) {
            val cursorObjectId = ObjectId(cursor)
            criteria.and("_id").lt(cursorObjectId)
        }

        // 2. 피드를 최신 순(DESC)으로 정렬하고 size만큼 제한
        val query = Query(criteria)
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
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
