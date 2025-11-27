package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleCommentRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import java.util.*

@Component
class MongoScheduleCommentRepository(
    private val mongoTemplate: MongoTemplate
) : ScheduleCommentRepository {

    override fun save(scheduleComment: ScheduleComment): ScheduleComment {
        return mongoTemplate.save(scheduleComment)
    }

    override fun findById(id: ObjectId): ScheduleComment? {
        val result = mongoTemplate.findById(id, ScheduleComment::class.java)
        return result
    }

    override fun findByMemberId(memberId: ObjectId): List<ScheduleComment> {
        val query = Query(Criteria.where("writer.id").`is`(memberId))
        return mongoTemplate.find(query, ScheduleComment::class.java)
    }

    override fun findByScheduleId(
        scheduleId: ObjectId,
        cursor: ObjectId?,
        size: Long
    ): List<ScheduleComment> {
        val criteria = Criteria.where("scheduleId").`is`(scheduleId)

        if (cursor != null) {
            criteria.and("_id").lt(cursor)
        }

        val query = Query(criteria).limit(size.toInt()).with(Sort.by(Sort.Direction.DESC, "id"))
        return mongoTemplate.find(query, ScheduleComment::class.java)
    }

    override fun deleteById(id: ObjectId) {
        val query = Query(Criteria.where("_id").`is`(id))
        mongoTemplate.remove(query, ScheduleComment::class.java)
    }

    override fun updateWriterInfo(member: Member): Int {
        return try {
            val writerId = member.id!!
            val query = Query(Criteria.where("writer._id").`is`(writerId))
            val update = Update().set("writer", member)
            val result = mongoTemplate.updateMulti(query, update, ScheduleComment::class.java)
            result.modifiedCount.toInt()
        } catch (e: Exception) {
            0
        }
    }
}
