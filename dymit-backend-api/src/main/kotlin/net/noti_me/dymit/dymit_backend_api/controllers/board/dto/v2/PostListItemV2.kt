package net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory

/**
 * 게시글 V2 목록 아이템 DTO입니다.
 */
@Schema(description = "게시글 V2 목록 아이템 DTO")
class PostListItemV2(
    val id: String,
    val groupId: String,
    val boardId: String,
    val title: String,
    val writer: WriterVo,
    val category: PostCategory,
    val scheduleId: String?,
    val createdAt: String,
    val commentCount: Long
) {

    companion object {
        fun from(dto: PostDtoV2): PostListItemV2 {
            return PostListItemV2(
                id = dto.id,
                groupId = dto.groupId,
                boardId = dto.boardId,
                title = dto.title,
                writer = WriterVo(
                    memberId = dto.writer.id.toHexString(),
                    nickname = dto.writer.nickname,
                    image = dto.writer.image
                ),
                category = dto.category,
                scheduleId = dto.scheduleId,
                createdAt = dto.createdAt.toString(),
                commentCount = dto.commentCount
            )
        }
    }
}
