package net.noti_me.dymit.dymit_backend_api.controllers.board.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostDto
import net.noti_me.dymit.dymit_backend_api.domain.board.Post

@Schema(description = "게시글 목록 아이템 DTO")
class PostListItem(
    val id: String,
    val groupId:String,
    val boardId : String,
    val title: String,
    val writer: WriterVo,
    val createdAt: String,
    val commentCount: Long,
) {

    companion object {
        fun from(dto: PostDto): PostListItem {
            return PostListItem(
                id = dto.id,
                title = dto.title,
                groupId = dto.groupId,
                boardId = dto.boardId,
                writer = WriterVo(
                    memberId = dto.writer.id.toHexString(),
                    nickname = dto.writer.nickname,
                    image = dto.writer.image
                ),
                createdAt = dto.createdAt.toString(),
                commentCount = dto.commentCount
            )
        }
    }
}