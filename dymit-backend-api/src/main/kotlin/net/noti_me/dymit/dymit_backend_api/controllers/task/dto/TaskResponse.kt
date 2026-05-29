package net.noti_me.dymit.dymit_backend_api.controllers.task.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAttachmentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
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
    @field:Schema(description = "첨부 목록")
    val attachments: List<TaskAttachmentResponse>,
    @field:Schema(description = "마감 시각")
    val expireAt: LocalDateTime,
    @field:Schema(description = "대상자 상태 목록")
    val assignees: List<TaskAssigneeSummaryResponse>
) : BaseResponse() {

    companion object {
        fun from(dto: TaskDto): TaskResponse {
            return TaskResponse(
                taskId = dto.taskId,
                relatedScheduleId = dto.relatedScheduleId,
                type = dto.type,
                title = dto.title,
                description = dto.description,
                attachments = dto.attachments.map { TaskAttachmentResponse.from(it) },
                expireAt = dto.expireAt,
                assignees = dto.assignees.map { TaskAssigneeSummaryResponse.from(it) }
            )
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
    val status: UserFileStatus
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
    val profileImageType: ProfileImageType,
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
