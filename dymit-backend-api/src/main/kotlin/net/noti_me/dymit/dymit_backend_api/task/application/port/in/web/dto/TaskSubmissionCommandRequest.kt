package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionAttachmentCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmitAttachmentType

@Schema(description = "과제 제출 생성/수정 요청")
class TaskSubmissionCommandRequest(
    @field:Schema(description = "제출 제목", example = "1회차 사전 과제 제출")
    @field:NotBlank(message = "제출 제목은 비어 있을 수 없습니다.")
    val title: String,
    @field:Schema(description = "제출 내용", example = "과제 정리 내용입니다.")
    val content: String,
    @field:Schema(description = "제출 첨부 목록")
    @field:Valid
    val attachments: List<TaskSubmissionAttachmentRequest> = emptyList()
) {

    fun toCreateCommand(): CreateTaskSubmissionCommand {
        return CreateTaskSubmissionCommand(
            title = title,
            content = content,
            attachments = attachments.map { it.toCommand() }
        )
    }

    fun toUpdateCommand(): UpdateTaskSubmissionCommand {
        return UpdateTaskSubmissionCommand(
            title = title,
            content = content,
            attachments = attachments.map { it.toCommand() }
        )
    }
}

@Schema(description = "과제 제출 첨부 요청")
class TaskSubmissionAttachmentRequest(
    @field:Schema(description = "첨부 타입", allowableValues = ["URL", "FILE"], example = "URL")
    val type: TaskSubmitAttachmentType,
    @field:Schema(description = "첨부 제목", example = "참고 자료")
    @field:NotBlank(message = "첨부 제목은 비어 있을 수 없습니다.")
    val title: String,
    @field:Schema(description = "URL 첨부 주소", example = "https://example.com")
    val url: String? = null,
    @field:Schema(description = "파일 첨부 ID", example = "682fabc1234567890abcdeff")
    val fileId: String? = null
) {

    fun toCommand(): TaskSubmissionAttachmentCommand {
        return TaskSubmissionAttachmentCommand(
            type = type,
            title = title,
            url = url,
            fileId = fileId
        )
    }
}
