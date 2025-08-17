package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class MongoStudyScheduleRepository(
    private val mongoTemplate: MongoTemplate
): StudyScheduleRepository {

    override fun save(schedule: StudySchedule): StudySchedule {
        return mongoTemplate.save(schedule)
    }

    override fun delete(schedule: StudySchedule): Boolean {
        return mongoTemplate.remove(schedule).deletedCount > 0
    }

    override fun deleteById(id: ObjectId): Boolean {
        val query = Query(Criteria.where("id").`is`(id))
        val result = mongoTemplate.remove(query, StudySchedule::class.java)
        return result.deletedCount > 0
    }

    override fun loadByGroupIdOrderByScheduleAtDesc(studyGroupId: ObjectId): List<StudySchedule> {
        val query = Query(Criteria.where("groupId").`is`(studyGroupId))
            .with(Sort.by(Sort.Direction.DESC, "scheduleAt"))
        return mongoTemplate.find(query, StudySchedule::class.java)
    }

    override fun loadById(id: ObjectId): StudySchedule? {
        return mongoTemplate.findById(id, StudySchedule::class.java)
    }

    override fun countByGroupId(studyGroupId: ObjectId): Long {
        val query = Query(Criteria.where("groupId").`is`(studyGroupId))
        return mongoTemplate.count(query, StudySchedule::class.java)
    }
}