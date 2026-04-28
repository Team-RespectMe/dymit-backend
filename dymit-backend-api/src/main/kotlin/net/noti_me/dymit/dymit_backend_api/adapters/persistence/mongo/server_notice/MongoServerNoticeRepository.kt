package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.server_notice

import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoServerNoticeRepository(
    private val mongoTemplate: MongoTemplate
): ServerNoticeRepository {

    override fun findById(noticeId: ObjectId): ServerNotice? {
        return mongoTemplate.findById(noticeId, ServerNotice::class.java)
    }

    override fun findAllByCursorIdOrderByIdDesc(cursorId: ObjectId?, pageSize: Int): List<ServerNotice> {
        val query = Query()
        if ( cursorId != null ) {
            query.addCriteria(Criteria.where("_id").lt(cursorId) )
        }
        query.limit(pageSize)
        query.with(Sort.by(Sort.Direction.DESC, "_id"))
        return mongoTemplate.find(query, ServerNotice::class.java)
    }

    override fun save(serverNotice: ServerNotice): ServerNotice {
        return mongoTemplate.save(serverNotice)
    }

    override fun deleteById(noticeId: ObjectId) {
        val serverNotice = findById(noticeId) ?: return
        mongoTemplate.remove(serverNotice)
    }

    override fun delete(serverNotice: ServerNotice) {
        mongoTemplate.remove(serverNotice)
    }
}