package net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleCommentWriter
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleComment
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.dto.ScheduleCommentWriterUpdateDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupProfileImageDto
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

    override fun updateWriterInfo(writer: ScheduleCommentWriterUpdateDto): Int {
        return try {
            val query = Query(Criteria.where("writer._id").`is`(writer.memberId))
            val update = Update().set(
                "writer",
                ScheduleCommentWriter(
                    id = writer.memberId,
                    nickname = writer.nickname,
                    image = StudyScheduleGroupProfileImageDto(
                        type = writer.profileImageType,
                        url = writer.profileImageUrl
                    )
                )
            )
            val result = mongoTemplate.updateMulti(query, update, ScheduleComment::class.java)
            result.modifiedCount.toInt()
        } catch (e: Exception) {
            0
        }
    }
}
