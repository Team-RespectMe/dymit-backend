package net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.ScheduleCommentDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import java.time.LocalDateTime

@Schema(description = "일정 댓글 응답")
class ScheduleCommentResponse(
    @Schema(description = "댓글 ID", example = "507f1f77bcf86cd799439012")
    val id: String,
    @Schema(description = "댓글 작성자 정보")
    val writer: WriterVo,
    @Schema(description = "댓글 생성 시간", example = "2025-08-29T10:30:00")
    val createdAt: LocalDateTime,
    @Schema(description = "댓글 내용", example = "이 일정에 참여하고 싶습니다!")
    val content: String,
): BaseResponse() {


    companion object {
        fun from(dto: ScheduleCommentDto): ScheduleCommentResponse {
            return ScheduleCommentResponse(
                id = dto.id,
                writer = dto.writer,
                createdAt = dto.createdAt,
                content = dto.content
            )
        }
    }
}
