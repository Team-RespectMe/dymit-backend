package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.task

import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskAssigneeRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * TaskAssignee Mongo 저장소 구현체입니다.
 */
@Repository
class MongoTaskAssigneeRepository(
    private val mongoTemplate: MongoTemplate
) : TaskAssigneeRepository {

    override fun save(taskAssignee: TaskAssignee): TaskAssignee {
        return mongoTemplate.save(taskAssignee)
    }

    override fun saveAll(taskAssignees: List<TaskAssignee>): List<TaskAssignee> {
        if ( taskAssignees.isEmpty() ) {
            return emptyList()
        }
        return mongoTemplate.insertAll(taskAssignees).map { it as TaskAssignee }
    }

    override fun findByTaskId(taskId: ObjectId): List<TaskAssignee> {
        val query = Query(Criteria.where("taskId").`is`(taskId))
        return mongoTemplate.find(query, TaskAssignee::class.java)
    }

    override fun findByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): TaskAssignee? {
        val query = Query(Criteria.where("taskId").`is`(taskId).and("memberId").`is`(memberId))
        return mongoTemplate.findOne(query, TaskAssignee::class.java)
    }

    override fun existsByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): Boolean {
        val query = Query(Criteria.where("taskId").`is`(taskId).and("memberId").`is`(memberId))
        return mongoTemplate.exists(query, TaskAssignee::class.java)
    }

    override fun deleteByTaskId(taskId: ObjectId): Long {
        val query = Query(Criteria.where("taskId").`is`(taskId))
        return mongoTemplate.remove(query, TaskAssignee::class.java).deletedCount
    }

    override fun deleteByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): Boolean {
        val query = Query(Criteria.where("taskId").`is`(taskId).and("memberId").`is`(memberId))
        return mongoTemplate.remove(query, TaskAssignee::class.java).deletedCount > 0
    }
}
