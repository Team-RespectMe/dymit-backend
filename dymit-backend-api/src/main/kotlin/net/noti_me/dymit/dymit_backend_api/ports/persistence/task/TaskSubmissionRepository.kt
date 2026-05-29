package net.noti_me.dymit.dymit_backend_api.ports.persistence.task

import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import org.bson.types.ObjectId

/**
 * 과제 제출 영속성 포트입니다.
 */
interface TaskSubmissionRepository {

    fun save(taskSubmission: TaskSubmission): TaskSubmission

    fun findById(id: ObjectId): TaskSubmission?

    fun findByTaskId(taskId: ObjectId): List<TaskSubmission>

    fun findByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): TaskSubmission?

    fun findByTaskIdAndMemberIdIn(taskId: ObjectId, memberIds: List<ObjectId>): List<TaskSubmission>

    fun deleteById(id: ObjectId): Boolean

    fun deleteByTaskId(taskId: ObjectId): Long

    fun deleteByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): TaskSubmission?

    fun findAttachedFileIds(fileIds: List<ObjectId>): Set<ObjectId>

    fun findAttachedFileIdsExcludingSubmission(
        fileIds: List<ObjectId>,
        submissionId: ObjectId
    ): Set<ObjectId>

    fun findAttachedFileIdsByTaskIdAndMemberId(taskId: ObjectId, memberId: ObjectId): Set<ObjectId>
}
