package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoScheduleParticipantRepository(
    private val mongoTemplate: MongoTemplate
): ScheduleParticipantRepository {

    override fun save(participant: ScheduleParticipant): ScheduleParticipant {
        return mongoTemplate.save(participant)
    }

    override fun delete(participant: ScheduleParticipant): Boolean {
        return mongoTemplate.remove(participant).deletedCount > 0
    }

    override fun existsByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): Boolean {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId)
            .and("memberId").`is`(memberId))
        return mongoTemplate.exists(query, ScheduleParticipant::class.java)
    }

    override fun getByScheduleId(scheduleId: ObjectId): List<ScheduleParticipant> {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId))
        return mongoTemplate.find(query, ScheduleParticipant::class.java)
    }

    override fun getByScheduleIdAndMemberId(scheduleId: ObjectId, memberId: ObjectId): ScheduleParticipant? {
        val query = Query(Criteria.where("scheduleId").`is`(scheduleId)
            .and("memberId").`is`(memberId))
        return mongoTemplate.findOne(query, ScheduleParticipant::class.java)
    }
}