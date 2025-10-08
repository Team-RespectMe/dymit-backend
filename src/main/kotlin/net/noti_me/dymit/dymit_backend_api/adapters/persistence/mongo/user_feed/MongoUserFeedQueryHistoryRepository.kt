package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeedQueryHistory
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedQueryHistoryRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoUserFeedQueryHistoryRepository(
    private val mongoTemplate: MongoTemplate
) : UserFeedQueryHistoryRepository {

    override fun save(entity: UserFeedQueryHistory): UserFeedQueryHistory {
        return mongoTemplate.save(entity)
    }

    override fun findByMemberId(memberId: ObjectId): UserFeedQueryHistory? {
        val query = Query()
            .addCriteria(Criteria.where("memberId").`is`(memberId))
        return mongoTemplate.findOne(query, UserFeedQueryHistory::class.java)
    }
}