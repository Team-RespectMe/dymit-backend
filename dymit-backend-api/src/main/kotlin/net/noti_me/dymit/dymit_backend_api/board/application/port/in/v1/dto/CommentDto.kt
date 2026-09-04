package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto

import net.noti_me.dymit.dymit_backend_api.board.domain.PostComment
import net.noti_me.dymit.dymit_backend_api.board.domain.Writer
import java.time.Instant

class CommentDto(
    val id: String,
    val postId: String,
    val writer: Writer,
    val content: String,
    val createdAt: Instant?
) {

    companion object {
        fun from(entity: PostComment): CommentDto {
            return CommentDto(
                id = entity.identifier,
                postId = entity.postId.toHexString(),
                writer = entity.writer,
                content = entity.content,
                createdAt = entity.createdAt
            )
        }
    }
}
