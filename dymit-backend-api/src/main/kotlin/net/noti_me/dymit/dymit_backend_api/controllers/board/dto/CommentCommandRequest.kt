package net.noti_me.dymit.dymit_backend_api.controllers.board.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize

@Schema(description = "댓글 명령 요청 DTO")
@Sanitize
class CommentCommandRequest(
    @Schema(description = "댓글 내용", example = "이것은 댓글입니다.")
    val content: String
) {
}