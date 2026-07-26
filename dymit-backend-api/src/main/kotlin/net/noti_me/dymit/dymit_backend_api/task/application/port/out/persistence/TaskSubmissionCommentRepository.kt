package net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence

import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionComment
import org.bson.types.ObjectId

/**
 * 과제 제출 댓글 영속성 포트입니다.
 */
interface TaskSubmissionCommentRepository {

    fun save(comment: TaskSubmissionComment): TaskSubmissionComment

    fun findById(id: ObjectId): TaskSubmissionComment?

    fun findBySubmissionId(submissionId: ObjectId): List<TaskSubmissionComment>

    fun deleteById(id: ObjectId): Boolean

    fun deleteBySubmissionId(submissionId: ObjectId): Long

    fun deleteByTaskId(taskId: ObjectId): Long
}
