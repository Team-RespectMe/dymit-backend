package net.noti_me.dymit.dymit_backend_api.task.adapter.`out`.persistence.mongo

import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionComment
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskSubmissionCommentRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * TaskSubmissionComment Mongo 저장소 구현체입니다.
 */
@Repository
class MongoTaskSubmissionCommentRepository(
    private val mongoTemplate: MongoTemplate
) : TaskSubmissionCommentRepository {

    override fun save(comment: TaskSubmissionComment): TaskSubmissionComment {
        return mongoTemplate.save(comment)
    }

    override fun findById(id: ObjectId): TaskSubmissionComment? {
        return mongoTemplate.findById(id, TaskSubmissionComment::class.java)
    }

    override fun findBySubmissionId(submissionId: ObjectId): List<TaskSubmissionComment> {
        val query = Query(Criteria.where("submissionId").`is`(submissionId))
            .with(Sort.by(Sort.Direction.ASC, "createdAt"))
        return mongoTemplate.find(query, TaskSubmissionComment::class.java)
    }

    override fun deleteById(id: ObjectId): Boolean {
        val query = Query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove(query, TaskSubmissionComment::class.java).deletedCount > 0
    }

    override fun deleteBySubmissionId(submissionId: ObjectId): Long {
        val query = Query(Criteria.where("submissionId").`is`(submissionId))
        return mongoTemplate.remove(query, TaskSubmissionComment::class.java).deletedCount
    }

    override fun deleteByTaskId(taskId: ObjectId): Long {
        val query = Query(Criteria.where("taskId").`is`(taskId))
        return mongoTemplate.remove(query, TaskSubmissionComment::class.java).deletedCount
    }
}
