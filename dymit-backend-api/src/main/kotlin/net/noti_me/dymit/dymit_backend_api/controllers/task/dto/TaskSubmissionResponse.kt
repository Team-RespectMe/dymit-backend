package net.noti_me.dymit.dymit_backend_api.controllers.task.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionAttachmentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachmentType
import java.time.LocalDateTime

@Schema(description = "과제 제출 응답")
class TaskSubmissionResponse(
    @field:Schema(description = "제출 ID")
    val submissionId: String,
    @field:Schema(description = "과제 ID")
    val taskId: String,
    @field:Schema(description = "작성 멤버 정보")
    val member: MemberVo,
    @field:Schema(description = "제출 제목")
    val title: String,
    @field:Schema(description = "제출 내용")
    val content: String,
    @field:Schema(description = "첨부 목록")
    val attachments: List<TaskSubmissionAttachmentResponse>,
    @field:Schema(description = "생성 시각")
    val createdAt: LocalDateTime?
) : BaseResponse() {

    @get:JsonIgnore
    val memberId: String
        get() = member.memberId

    @get:JsonIgnore
    val memberNickname: String
        get() = member.nickname

    @get:JsonIgnore
    val memberProfileImageUrl: String
        get() = member.profileImageUrl

    @get:JsonIgnore
    val memberProfileImageType: TaskProfileImageType
        get() = member.profileImageType

    companion object {
        fun from(dto: TaskSubmissionDto): TaskSubmissionResponse {
            return TaskSubmissionResponse(
                submissionId = dto.submissionId,
                taskId = dto.taskId,
                member = MemberVo(
                    memberId = dto.memberId,
                    nickname = dto.memberNickname,
                    profileImageUrl = dto.memberProfileImageUrl,
                    profileImageType = dto.memberProfileImageType
                ),
                title = dto.title,
                content = dto.content,
                attachments = dto.attachments.map { TaskSubmissionAttachmentResponse.from(it) },
                createdAt = dto.createdAt
            )
        }
    }
}

@Schema(description = "과제 제출 첨부 응답")
class TaskSubmissionAttachmentResponse(
    @field:Schema(description = "첨부 타입")
    val type: TaskSubmitAttachmentType,
    @field:Schema(description = "첨부 제목")
    val title: String,
    @field:Schema(description = "URL 첨부 주소")
    val url: String?,
    @field:Schema(description = "파일 첨부 ID")
    val fileId: String?,
    @field:Schema(description = "파일 첨부 URL")
    val fileUrl: String?,
    @field:Schema(description = "파일 원본명")
    val originalFileName: String?
) {

    companion object {
        fun from(dto: TaskSubmissionAttachmentDto): TaskSubmissionAttachmentResponse {
            return TaskSubmissionAttachmentResponse(
                type = dto.type,
                title = dto.title,
                url = dto.url,
                fileId = dto.fileId,
                fileUrl = dto.fileUrl,
                originalFileName = dto.originalFileName
            )
        }
    }
}
