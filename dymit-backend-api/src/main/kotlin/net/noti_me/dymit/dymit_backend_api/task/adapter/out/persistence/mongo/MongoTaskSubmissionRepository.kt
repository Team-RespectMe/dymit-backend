package net.noti_me.dymit.dymit_backend_api.task.adapter.`out`.persistence.mongo

import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskSubmissionRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * TaskSubmission Mongo 저장소 구현체입니다.
 */
@Repository
class MongoTaskSubmissionRepository(
    private val mongoTemplate: MongoTemplate
) : TaskSubmissionRepository {

    override fun save(taskSubmission: TaskSubmission): TaskSubmission {
        return mongoTemplate.save(taskSubmission)
    }

    override fun findById(id: ObjectId): TaskSubmission? {
        return mongoTemplate.findById(id, TaskSubmission::class.java)
    }

    override fun findByTaskId(taskId: ObjectId): List<TaskSubmission> {
        val query = Query(Criteria.where("taskId").`is`(taskId))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
        return mongoTemplate.find(query, TaskSubmission::class.java)
    }

    override fun findByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): TaskSubmission? {
        val query = Query(Criteria.where("taskId").`is`(taskId).and("memberId").`is`(memberId))
        return mongoTemplate.findOne(query, TaskSubmission::class.java)
    }

    override fun findByTaskIdAndMemberIdIn(taskId: ObjectId, memberIds: List<ObjectId>): List<TaskSubmission> {
        if ( memberIds.isEmpty() ) {
            return emptyList()
        }
        val query = Query(Criteria.where("taskId").`is`(taskId).and("memberId").`in`(memberIds))
        return mongoTemplate.find(query, TaskSubmission::class.java)
    }

    override fun deleteById(id: ObjectId): Boolean {
        val query = Query(Criteria.where("_id").`is`(id))
        return mongoTemplate.remove(query, TaskSubmission::class.java).deletedCount > 0
    }

    override fun deleteByTaskId(taskId: ObjectId): Long {
        val query = Query(Criteria.where("taskId").`is`(taskId))
        return mongoTemplate.remove(query, TaskSubmission::class.java).deletedCount
    }

    override fun deleteByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): TaskSubmission? {
        val submission = findByTaskIdAndMemberId(taskId, memberId) ?: return null
        deleteById(submission.id!!)
        return submission
    }

    override fun findAttachedFileIds(fileIds: List<ObjectId>): Set<ObjectId> {
        if ( fileIds.isEmpty() ) {
            return emptySet()
        }

        val query = Query(
            Criteria.where("attachments.fileId").`in`(fileIds)
                .and("attachments.type").`is`(TaskSubmitAttachmentType.FILE)
        )

        return mongoTemplate.find(query, TaskSubmission::class.java)
            .flatMap { it.attachments }
            .filter { it.type == TaskSubmitAttachmentType.FILE && it.fileId != null }
            .map { it.fileId!! }
            .filter { fileIds.contains(it) }
            .toSet()
    }

    override fun findAttachedFileIdsExcludingSubmission(
        fileIds: List<ObjectId>,
        submissionId: ObjectId
    ): Set<ObjectId> {
        if ( fileIds.isEmpty() ) {
            return emptySet()
        }

        val query = Query(
            Criteria.where("attachments.fileId").`in`(fileIds)
                .and("attachments.type").`is`(TaskSubmitAttachmentType.FILE)
                .and("_id").ne(submissionId)
        )

        return mongoTemplate.find(query, TaskSubmission::class.java)
            .flatMap { it.attachments }
            .filter { it.type == TaskSubmitAttachmentType.FILE && it.fileId != null }
            .map { it.fileId!! }
            .filter { fileIds.contains(it) }
            .toSet()
    }

    override fun findAttachedFileIdsByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): Set<ObjectId> {
        val submission = findByTaskIdAndMemberId(taskId, memberId) ?: return emptySet()
        return submission.attachments
            .filter { it.type == TaskSubmitAttachmentType.FILE && it.fileId != null }
            .map { it.fileId!! }
            .toSet()
    }
}
