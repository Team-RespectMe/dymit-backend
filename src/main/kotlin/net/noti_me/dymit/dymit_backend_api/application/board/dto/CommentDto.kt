package net.noti_me.dymit.dymit_backend_api.application.board.dto

import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer

class CommentDto(
    val id: String,
    val postId: String,
    val writer: Writer,
    val content: String,
    val createdAt: String
) {

    companion object {
        fun from(entity: PostComment): CommentDto {
            return CommentDto(
                id = entity.id.toHexString(),
                postId = entity.postId.toHexString(),
                writer = entity.writer,
                content = entity.content,
                createdAt = entity.createdAt.toString()
            )
        }
    }
}