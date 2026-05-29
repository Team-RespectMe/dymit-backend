package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.task

import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * Task Mongo 저장소 구현체입니다.
 */
@Repository
class MongoTaskRepository(
    private val mongoTemplate: MongoTemplate
) : TaskRepository {

    override fun save(task: Task): Task {
        return mongoTemplate.save(task)
    }

    override fun findById(id: ObjectId): Task? {
        return mongoTemplate.findById(id, Task::class.java)
    }

    override fun findByRelatedScheduleId(scheduleId: ObjectId): List<Task> {
        val query = Query(Criteria.where("relatedScheduleId").`is`(scheduleId))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
        return mongoTemplate.find(query, Task::class.java)
    }

    override fun findByRelatedScheduleIdAndType(
        scheduleId: ObjectId,
        type: TaskType
    ): List<Task> {
        val query = Query(
            Criteria.where("relatedScheduleId").`is`(scheduleId)
                .and("type").`is`(type)
        )
        return mongoTemplate.find(query, Task::class.java)
    }

    override fun findByRelatedScheduleIds(scheduleIds: List<ObjectId>): List<Task> {
        if ( scheduleIds.isEmpty() ) {
            return emptyList()
        }
        val query = Query(Criteria.where("relatedScheduleId").`in`(scheduleIds))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
        return mongoTemplate.find(query, Task::class.java)
    }

    override fun deleteById(id: ObjectId): Boolean {
        val query = Query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove(query, Task::class.java).deletedCount > 0
    }

    override fun findAttachedFileIds(fileIds: List<ObjectId>): Set<ObjectId> {
        if ( fileIds.isEmpty() ) {
            return emptySet()
        }

        val query = Query(Criteria.where("attachments.fileId").`in`(fileIds))
        return mongoTemplate.find(query, Task::class.java)
            .flatMap { task -> task.attachments.map { it.fileId } }
            .filter { fileIds.contains(it) }
            .toSet()
    }

    override fun findAttachedFileIdsExcludingTask(
        fileIds: List<ObjectId>,
        taskId: ObjectId
    ): Set<ObjectId> {
        if ( fileIds.isEmpty() ) {
            return emptySet()
        }

        val query = Query(
            Criteria.where("attachments.fileId").`in`(fileIds)
                .and("_id").ne(taskId)
        )
        return mongoTemplate.find(query, Task::class.java)
            .flatMap { task -> task.attachments.map { it.fileId } }
            .filter { fileIds.contains(it) }
            .toSet()
    }
}
