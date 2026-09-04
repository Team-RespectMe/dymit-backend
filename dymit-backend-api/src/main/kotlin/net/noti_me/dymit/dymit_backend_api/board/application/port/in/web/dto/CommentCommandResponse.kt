package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.CommentDto
import java.time.Instant

@Schema(description = "댓글 커맨드 응답")
data class CommentCommandResponse(
    @Schema(description = "댓글 ID", example = "64f1b2a3c4d5e6f7a8b9c1d2")
    val id: String,
    @Schema(description = "게시물 ID", example = "64f1b2a3c4d5e6f7a8b9c1d3")
    val postId: String,
    @Schema(description = "작성자 정보")
    val writer: WriterVo,
    @Schema(description = "댓글 내용", example = "이것은 댓글 내용입니다.")
    val content: String,
    @Schema(description = "생성일시", example = "2024-01-01T12:00:00")
    val createdAt: Instant?
) {
    companion object {
        fun from(commentDto: CommentDto): CommentCommandResponse {
            return CommentCommandResponse(
                id = commentDto.id,
                postId = commentDto.postId,
                writer = WriterVo.from(commentDto.writer),
                content = commentDto.content,
                createdAt = commentDto.createdAt
            )
        }
    }
}
