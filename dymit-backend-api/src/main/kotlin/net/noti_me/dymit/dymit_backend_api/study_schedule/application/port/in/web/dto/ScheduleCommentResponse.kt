package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.ScheduleCommentDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import java.time.Instant

@Schema(description = "일정 댓글 응답")
class ScheduleCommentResponse(
    @Schema(description = "댓글 ID", example = "507f1f77bcf86cd799439012")
    val id: String,
    @Schema(description = "댓글 작성자 정보")
    val writer: ScheduleCommentWriterResponse,
    @Schema(description = "댓글 생성 시간", example = "2025-08-29T10:30:00")
    val createdAt: Instant,
    @Schema(description = "댓글 내용", example = "이 일정에 참여하고 싶습니다!")
    val content: String,
): BaseResponse() {


    companion object {
        fun from(dto: ScheduleCommentDto): ScheduleCommentResponse {
            return ScheduleCommentResponse(
                id = dto.id,
                writer = ScheduleCommentWriterResponse(
                    memberId = dto.writer.memberId,
                    nickname = dto.writer.nickname,
                    image = StudyScheduleProfileImageResponse.of(
                        dto.writer.image.type,
                        dto.writer.image.url
                    )
                ),
                createdAt = dto.createdAt,
                content = dto.content
            )
        }
    }
}

class ScheduleCommentWriterResponse(
    val memberId: String,
    val nickname: String,
    val image: StudyScheduleProfileImageResponse
)
