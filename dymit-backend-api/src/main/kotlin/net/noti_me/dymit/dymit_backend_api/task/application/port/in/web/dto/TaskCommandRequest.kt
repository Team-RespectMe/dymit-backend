package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import java.time.Instant

@Schema(description = "과제 생성 요청")
class TaskCommandRequest(
    @field:Schema(description = "연관 일정 ID", example = "682fabc1234567890abcdeff")
    val relatedScheduleId: String,
    @field:Schema(description = "과제 제목", example = "1회차 사전 과제")
    @field:NotBlank(message = "과제 제목은 비어 있을 수 없습니다.")
    val title: String,
    @field:Schema(description = "과제 설명", example = "이번 주 학습 범위를 정리해 주세요.")
    val description: String,
    @field:Schema(description = "과제 첨부 파일 ID 목록", example = "[\"682fabc1234567890abcdeff\"]")
    val attachmentFileIds: List<String> = emptyList(),
    @field:Schema(description = "제출 마감 시각", example = "2030-06-01T23:59:59")
    val expireAt: Instant,
    @field:Schema(description = "과제 대상자 멤버 ID 목록. POST 과제에서만 사용됩니다.", example = "[\"682fabc1234567890abcdeff\"]")
    val assigneeMemberIds: List<String> = emptyList(),
    @field:Schema(description = "과제 제출 방식", allowableValues = ["CHECK", "OUTPUT"], example = "OUTPUT")
    val submissionType: TaskSubmissionType = TaskSubmissionType.OUTPUT
) {

    fun toCreateCommand(): CreateTaskCommand {
        return CreateTaskCommand(
            relatedScheduleId = relatedScheduleId,
            title = title,
            description = description,
            attachmentFileIds = attachmentFileIds,
            expireAt = expireAt,
            assigneeMemberIds = assigneeMemberIds,
            submissionType = submissionType
        )
    }
}
