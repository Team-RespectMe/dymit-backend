package net.noti_me.dymit.dymit_backend_api.controllers.board.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize

@Schema(description = "게시글 생성/수정 요청 DTO")
@Sanitize
class PostCommandRequest(
    @field: NotEmpty(message = "제목은 비어 있을 수 없습니다.")
    @Schema(description = "게시글 제목", example = "첫 번째 게시글입니다.")
    val title: String,
    @field: NotEmpty(message = "내용은 비어 있을 수 없습니다.")
    @Schema(description = "게시글 내용", example = "안녕하세요, 첫 번째 게시글입니다.")
    val content: String,
) {

    fun toCommand(groupId: String, boardId: String): PostCommand {
        return PostCommand(
            title = this.title,
            groupId = groupId,
            boardId = boardId,
            content = this.content
        )
    }
}