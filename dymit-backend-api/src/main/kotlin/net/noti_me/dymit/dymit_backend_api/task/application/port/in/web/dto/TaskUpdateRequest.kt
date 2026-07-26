package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskCommand
import java.time.LocalDateTime

@Schema(description = "과제 수정 요청")
class TaskUpdateRequest(
    @field:Schema(description = "과제 제목", example = "1회차 사전 과제 수정")
    @field:NotBlank(message = "과제 제목은 비어 있을 수 없습니다.")
    val title: String,
    @field:Schema(description = "과제 설명", example = "요구사항을 보강해 주세요.")
    val description: String,
    @field:Schema(description = "과제 첨부 파일 ID 목록", example = "[\"682fabc1234567890abcdeff\"]")
    val attachmentFileIds: List<String> = emptyList(),
    @field:Schema(description = "제출 마감 시각", example = "2030-06-01T23:59:59")
    val expireAt: LocalDateTime,
    @field:Schema(description = "과제 대상자 멤버 ID 목록. null이면 대상자를 변경하지 않습니다.", example = "[\"682fabc1234567890abcdeff\"]")
    val assigneeMemberIds: List<String>? = null
) {

    /**
     * 과제 수정 요청을 애플리케이션 커맨드로 변환합니다.
     *
     * @return 과제 수정 커맨드
     */
    fun toCommand(): UpdateTaskCommand {
        return UpdateTaskCommand(
            title = title,
            description = description,
            attachmentFileIds = attachmentFileIds,
            expireAt = expireAt,
            assigneeMemberIds = assigneeMemberIds
        )
    }
}
