package net.noti_me.dymit.dymit_backend_api.application.task.dto

import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionType
import java.time.LocalDateTime

/**
 * 과제 생성 커맨드입니다.
 */
data class CreateTaskCommand(
    val relatedScheduleId: String,
    val title: String,
    val description: String,
    val attachmentFileIds: List<String>,
    val expireAt: LocalDateTime,
    val assigneeMemberIds: List<String> = emptyList(),
    val submissionType: TaskSubmissionType = TaskSubmissionType.OUTPUT
)

/**
 * 과제 수정 커맨드입니다.
 */
data class UpdateTaskCommand(
    val title: String,
    val description: String,
    val attachmentFileIds: List<String>,
    val expireAt: LocalDateTime
)

/**
 * 과제 제출 생성 커맨드입니다.
 */
data class CreateTaskSubmissionCommand(
    val title: String,
    val content: String,
    val attachments: List<TaskSubmissionAttachmentCommand>
)

/**
 * 과제 제출 수정 커맨드입니다.
 */
data class UpdateTaskSubmissionCommand(
    val title: String,
    val content: String,
    val attachments: List<TaskSubmissionAttachmentCommand>
)

/**
 * 과제 제출 첨부 커맨드입니다.
 */
data class TaskSubmissionAttachmentCommand(
    val type: TaskSubmitAttachmentType,
    val title: String,
    val url: String? = null,
    val fileId: String? = null
)

/**
 * 과제 댓글 생성 커맨드입니다.
 */
data class CreateTaskSubmissionCommentCommand(
    val content: String
)

/**
 * 과제 댓글 수정 커맨드입니다.
 */
data class UpdateTaskSubmissionCommentCommand(
    val content: String
)
