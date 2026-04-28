package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.GroupFeedRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoGroupFeedRepository(
    private val mongoTemplate: MongoTemplate
): GroupFeedRepository {

    override fun save(groupFeed: GroupFeed): GroupFeed {
        return mongoTemplate.save(groupFeed)
    }

    override fun findById(id: ObjectId): GroupFeed? {
        return mongoTemplate.findById(id, GroupFeed::class.java)
    }

    override fun findByGroupIdsOrderByIdDesc(groupIds: List<ObjectId>, cursor: ObjectId?, size: Long): List<GroupFeed> {
        val query = Query()
            .addCriteria(
                Criteria.where("groupId").`in`(groupIds)
            )
            .limit(size.toInt())
            .with(Sort.by(Direction.DESC, "_id"))

        cursor?.let { query.addCriteria(Criteria.where("_id").gt(cursor))}
        return mongoTemplate.find(query, GroupFeed::class.java)
    }
}
