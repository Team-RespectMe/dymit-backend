package net.noti_me.dymit.dymit_backend_api.controllers.task.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommentCommand

@Schema(description = "과제 제출 댓글 생성/수정 요청")
class TaskSubmissionCommentCommandRequest(
    @field:Schema(description = "댓글 내용", example = "정리 좋습니다. 다음엔 예시를 더 추가해 주세요.")
    @field:NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
    val content: String
) {

    fun toCreateCommand(): CreateTaskSubmissionCommentCommand {
        return CreateTaskSubmissionCommentCommand(content = content)
    }

    fun toUpdateCommand(): UpdateTaskSubmissionCommentCommand {
        return UpdateTaskSubmissionCommentCommand(content = content)
    }
}
