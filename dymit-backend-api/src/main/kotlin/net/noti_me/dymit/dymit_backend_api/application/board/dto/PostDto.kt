package net.noti_me.dymit.dymit_backend_api.application.board.dto

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import java.time.LocalDateTime

class PostDto(
    val id: String,
    val groupId: String,
    val boardId: String,
    val writer: Writer,
    val title: String,
    val content: String,
    val commentCount: Long = 0,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(entity: Post): PostDto {
            return PostDto(
                id = entity.identifier,
                groupId = entity.groupId.toHexString(),
                boardId = entity.boardId.toHexString(),
                writer = entity.writer,
                title = entity.title,
                content = entity.content,
                commentCount = entity.commentCount,
                createdAt = entity.createdAt?: LocalDateTime.now()
            )
        }
    }
}