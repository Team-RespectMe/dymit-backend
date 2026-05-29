package net.noti_me.dymit.dymit_backend_api.domain.task

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 과제 제출 댓글 엔티티입니다.
 */
@Document(collection = "task_submission_comments")
class TaskSubmissionComment(
    @Indexed(name = "task_submission_comment_task_id_idx")
    val taskId: ObjectId,
    @Indexed(name = "task_submission_comment_submission_id_idx")
    val submissionId: ObjectId,
    @Indexed(name = "task_submission_comment_writer_id_idx")
    val writerId: ObjectId,
    content: String,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false,
    id: ObjectId? = null
) : BaseAggregateRoot<TaskSubmissionComment>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    var content: String = content
        private set

    init {
        validate(content)
    }

    /**
     * 댓글 내용을 수정합니다.
     */
    fun update(requesterId: ObjectId, content: String) {
        if ( requesterId != writerId ) {
            throw ForbiddenException(message = "댓글 작성자만 수정할 수 있습니다.")
        }

        validate(content)
        this.content = content
        modified = true
    }

    /**
     * 댓글 삭제 권한을 검증합니다.
     */
    fun checkDeletePermission(requesterId: ObjectId) {
        if ( requesterId != writerId ) {
            throw ForbiddenException(message = "댓글 작성자만 삭제할 수 있습니다.")
        }
    }

    private fun validate(content: String) {
        if ( content.isBlank() ) {
            throw BadRequestException(message = "댓글 내용은 비어 있을 수 없습니다.")
        }

        if ( content.length > 500 ) {
            throw BadRequestException(message = "댓글은 500자 이하로 작성해야 합니다.")
        }
    }
}
