package net.noti_me.dymit.dymit_backend_api.domain.task

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 과제 제출 엔티티입니다.
 */
@Document(collection = "task_submissions")
class TaskSubmission(
    @Indexed(name = "task_submission_task_id_idx")
    val taskId: ObjectId,
    @Indexed(name = "task_submission_member_id_idx")
    val memberId: ObjectId,
    title: String,
    content: String,
    attachments: List<TaskSubmitAttachment>,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false,
    id: ObjectId? = null
) : BaseAggregateRoot<TaskSubmission>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    var title: String = title
        private set

    var content: String = content
        private set

    var attachments: MutableList<TaskSubmitAttachment> = attachments.toMutableList()
        private set

    init {
        validate(
            title = title,
            content = content,
            attachments = attachments
        )
    }

    /**
     * 제출 본문을 수정합니다.
     */
    fun update(
        title: String,
        content: String,
        attachments: List<TaskSubmitAttachment>
    ) {
        validate(
            title = title,
            content = content,
            attachments = attachments
        )

        this.title = title
        this.content = content
        this.attachments = attachments.toMutableList()
        modified = true
    }

    private fun validate(
        title: String,
        content: String,
        attachments: List<TaskSubmitAttachment>
    ) {
        if ( title.isBlank() ) {
            throw BadRequestException(message = "제출 제목은 비어 있을 수 없습니다.")
        }

        if ( title.length >= 255 ) {
            throw BadRequestException(message = "제출 제목은 255자 미만이어야 합니다.")
        }

        if ( content.length > 4000 ) {
            throw BadRequestException(message = "제출 내용은 4000자 이하로 작성해야 합니다.")
        }

        if ( attachments.size > 5 ) {
            throw BadRequestException(message = "제출 첨부는 최대 5개까지 가능합니다.")
        }
    }
}
