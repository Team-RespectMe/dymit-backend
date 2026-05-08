package net.noti_me.dymit.dymit_backend_api.application.board.v2.dto

import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import java.time.LocalDateTime

/**
 * 게시글 V2 응답 DTO입니다.
 */
class PostDtoV2(
    val id: String,
    val groupId: String,
    val boardId: String,
    val writer: Writer,
    val title: String,
    val content: String,
    val category: PostCategory,
    val scheduleId: String?,
    val commentCount: Long = 0,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(entity: Post): PostDtoV2 {
            return PostDtoV2(
                id = entity.identifier,
                groupId = entity.groupId.toHexString(),
                boardId = entity.boardId.toHexString(),
                writer = entity.writer,
                title = entity.title,
                content = entity.content,
                category = entity.category,
                scheduleId = entity.scheduleId?.toHexString(),
                commentCount = entity.commentCount,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}
