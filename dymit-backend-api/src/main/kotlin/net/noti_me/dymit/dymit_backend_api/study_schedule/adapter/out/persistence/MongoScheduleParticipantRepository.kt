package net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleParticipantRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class MongoScheduleParticipantRepository(
    private val mongoTemplate: MongoTemplate
): ScheduleParticipantRepository {

    override fun save(participant: ScheduleParticipant): ScheduleParticipant {
        return mongoTemplate.save(participant)
    }

    override fun delete(participant: ScheduleParticipant): Boolean {
        val now = java.time.Instant.now()
        val result = mongoTemplate.updateFirst(
            Query(
                Criteria.where("_id").`is`(participant.id)
                    .and("isDeleted").ne(true)
            ),
            Update()
                .set("isDeleted", true)
                .set("deletedAt", now)
                .set("updatedAt", now),
            ScheduleParticipant::class.java
        )
        return result.modifiedCount > 0
    }

    override fun existsByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): Boolean {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId)
            .and("memberId").`is`(memberId)
            .and("isDeleted").ne(true))
        return mongoTemplate.exists(query, ScheduleParticipant::class.java)
    }

    override fun getByScheduleId(scheduleId: ObjectId): List<ScheduleParticipant> {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId).and("isDeleted").ne(true))
        return mongoTemplate.find(query, ScheduleParticipant::class.java)
    }

    override fun getByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): ScheduleParticipant? {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId)
            .and("memberId").`is`(memberId)
            .and("isDeleted").ne(true))
        return mongoTemplate.findOne(query, ScheduleParticipant::class.java)
    }
}
