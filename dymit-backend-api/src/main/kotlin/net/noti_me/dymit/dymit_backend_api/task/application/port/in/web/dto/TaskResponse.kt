package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeSummaryDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAttachmentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.common.response.HateoasLink
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import java.time.LocalDateTime

@Schema(description = "과제 응답")
class TaskResponse(
    @field:Schema(description = "과제 ID")
    val taskId: String,
    @field:Schema(description = "연관 일정 ID")
    val relatedScheduleId: String,
    @field:Schema(description = "과제 타입")
    val type: TaskType,
    @field:Schema(description = "과제 제목")
    val title: String,
    @field:Schema(description = "과제 설명")
    val description: String,
    @field:Schema(description = "과제 제출 방식")
    val submissionType: TaskSubmissionType,
    @field:Schema(description = "첨부 목록")
    val attachments: List<TaskAttachmentResponse>,
    @field:Schema(description = "마감 시각")
    val expireAt: LocalDateTime,
    @field:Schema(description = "제출 완료 대상자 수")
    val submittedAssigneeCount: Int,
    @field:Schema(description = "미제출 대상자 수")
    val notSubmittedAssigneeCount: Int,
    @field:Schema(description = "대상자 상태 목록")
    val assignees: List<TaskAssigneeSummaryResponse>
) : BaseResponse() {

    companion object {
        /**
         * 과제 조회 DTO를 응답으로 변환합니다.
         *
         * @param dto 과제 조회 DTO
         * @param groupId 스터디 그룹 ID
         * @return 과제 응답
         */
        fun from(dto: TaskDto, groupId: String): TaskResponse {
            return TaskResponse(
                taskId = dto.taskId,
                relatedScheduleId = dto.relatedScheduleId,
                type = dto.type,
                title = dto.title,
                description = dto.description,
                submissionType = dto.submissionType,
                attachments = dto.attachments.map { TaskAttachmentResponse.from(it) },
                expireAt = dto.expireAt,
                submittedAssigneeCount = dto.submittedAssigneeCount,
                notSubmittedAssigneeCount = dto.notSubmittedAssigneeCount,
                assignees = dto.assignees.map { TaskAssigneeSummaryResponse.from(it) }
            ).also { response ->
                response._links["self"] = HateoasLink("/api/v1/study-groups/$groupId/tasks/${dto.taskId}")
            }
        }
    }
}

@Schema(description = "과제 첨부 응답")
class TaskAttachmentResponse(
    @field:Schema(description = "파일 ID")
    val fileId: String,
    @field:Schema(description = "원본 파일명")
    val originalFileName: String,
    @field:Schema(description = "파일 URL")
    val url: String,
    @field:Schema(description = "썸네일 URL")
    val thumbnailUrl: String?,
    @field:Schema(description = "파일 상태")
    val status: TaskFileStatusDto
) {

    companion object {
        fun from(dto: TaskAttachmentDto): TaskAttachmentResponse {
            return TaskAttachmentResponse(
                fileId = dto.fileId,
                originalFileName = dto.originalFileName,
                url = dto.url,
                thumbnailUrl = dto.thumbnailUrl,
                status = dto.status
            )
        }
    }
}

@Schema(description = "과제 대상자 상태 응답")
class TaskAssigneeSummaryResponse(
    @field:Schema(description = "멤버 ID")
    val memberId: String,
    @field:Schema(description = "닉네임")
    val nickname: String,
    @field:Schema(description = "프로필 이미지 URL")
    val profileImageUrl: String,
    @field:Schema(description = "프로필 이미지 타입")
    val profileImageType: TaskProfileImageType,
    @field:Schema(description = "제출 상태")
    val status: TaskAssigneeStatus
) {

    companion object {
        fun from(dto: TaskAssigneeSummaryDto): TaskAssigneeSummaryResponse {
            return TaskAssigneeSummaryResponse(
                memberId = dto.memberId,
                nickname = dto.nickname,
                profileImageUrl = dto.profileImageUrl,
                profileImageType = dto.profileImageType,
                status = dto.status
            )
        }
    }
}
