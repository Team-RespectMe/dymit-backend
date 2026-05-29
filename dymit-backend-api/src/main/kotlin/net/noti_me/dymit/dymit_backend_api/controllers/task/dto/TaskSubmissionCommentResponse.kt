package net.noti_me.dymit.dymit_backend_api.controllers.task.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import java.time.LocalDateTime

@Schema(description = "과제 제출 댓글 응답")
class TaskSubmissionCommentResponse(
    @field:Schema(description = "댓글 ID")
    val commentId: String,
    @field:Schema(description = "과제 ID")
    val taskId: String,
    @field:Schema(description = "제출 ID")
    val submissionId: String,
    @field:Schema(description = "작성자 정보")
    val writer: MemberVo,
    @field:Schema(description = "댓글 내용")
    val content: String,
    @field:Schema(description = "생성 시각")
    val createdAt: LocalDateTime?
) : BaseResponse() {

    @get:JsonIgnore
    val writerId: String
        get() = writer.memberId

    @get:JsonIgnore
    val writerNickname: String
        get() = writer.nickname

    @get:JsonIgnore
    val writerProfileImageUrl: String
        get() = writer.profileImageUrl

    @get:JsonIgnore
    val writerProfileImageType: ProfileImageType
        get() = writer.profileImageType

    companion object {
        fun from(dto: TaskSubmissionCommentDto): TaskSubmissionCommentResponse {
            return TaskSubmissionCommentResponse(
                commentId = dto.commentId,
                taskId = dto.taskId,
                submissionId = dto.submissionId,
                writer = MemberVo(
                    memberId = dto.writerId,
                    nickname = dto.writerNickname,
                    profileImageUrl = dto.writerProfileImageUrl,
                    profileImageType = dto.writerProfileImageType
                ),
                content = dto.content,
                createdAt = dto.createdAt
            )
        }
    }
}
