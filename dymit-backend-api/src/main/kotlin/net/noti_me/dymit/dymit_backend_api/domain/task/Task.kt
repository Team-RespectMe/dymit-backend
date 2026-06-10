package net.noti_me.dymit.dymit_backend_api.domain.task

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 스터디 과제 엔티티입니다.
 *
 * @param relatedScheduleId 연관 일정 ID
 * @param type 과제 타입
 * @param title 과제 제목
 * @param description 과제 설명
 * @param attachments 과제 첨부 파일 목록
 * @param expireAt 제출 마감 시각
 * @param submissionType 과제 제출 방식
 */
@Document(collection = "tasks")
class Task(
    @Indexed(name = "task_related_schedule_id_idx")
    val relatedScheduleId: ObjectId,
    type: TaskType,
    title: String,
    description: String,
    attachments: List<TaskAttachment>,
    expireAt: LocalDateTime,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false,
    id: ObjectId? = null,
    submissionType: TaskSubmissionType = TaskSubmissionType.OUTPUT
) : BaseAggregateRoot<Task>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    var type: TaskType = type
        private set

    var title: String = title
        private set

    var description: String = description
        private set

    var attachments: MutableList<TaskAttachment> = attachments.toMutableList()
        private set

    var expireAt: LocalDateTime = expireAt
        private set

    val submissionType: TaskSubmissionType = submissionType

    init {
        validate(
            title = title,
            description = description,
            attachments = attachments,
            expireAt = expireAt
        )
    }

    /**
     * 과제 내용을 수정합니다.
     */
    fun update(
        title: String,
        description: String,
        attachments: List<TaskAttachment>,
        expireAt: LocalDateTime
    ) {
        validate(
            title = title,
            description = description,
            attachments = attachments,
            expireAt = expireAt
        )

        this.title = title
        this.description = description
        this.attachments = attachments.toMutableList()
        this.expireAt = expireAt
        modified = true
    }

    private fun validate(
        title: String,
        description: String,
        attachments: List<TaskAttachment>,
        @Suppress("UNUSED_PARAMETER") expireAt: LocalDateTime
    ) {
        if ( title.isBlank() ) {
            throw BadRequestException(message = "과제 제목은 비어 있을 수 없습니다.")
        }

        if ( title.length >= 255 ) {
            throw BadRequestException(message = "과제 제목은 255자 미만이어야 합니다.")
        }

        if ( description.length > 4000 ) {
            throw BadRequestException(message = "과제 설명은 4000자 이하로 작성해야 합니다.")
        }

        if ( attachments.size > 5 ) {
            throw BadRequestException(message = "과제 첨부 파일은 최대 5개까지 가능합니다.")
        }
    }
}
