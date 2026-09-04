package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.v2

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto.WriterVo
import net.noti_me.dymit.dymit_backend_api.board.domain.PostCategory
import java.time.Instant

/**
 * 게시글 V2 상세 응답 DTO입니다.
 */
@Schema(description = "게시글 V2 상세 응답 DTO")
class PostDetailResponseV2(
    val id: String,
    val groupId: String,
    val boardId: String,
    val title: String,
    val content: String,
    val writer: WriterVo,
    val category: PostCategory,
    val scheduleId: String?,
    val commentCount: Long,
    val createdAt: Instant
) {

    companion object {
        fun from(dto: PostDtoV2): PostDetailResponseV2 {
            return PostDetailResponseV2(
                id = dto.id,
                groupId = dto.groupId,
                boardId = dto.boardId,
                title = dto.title,
                content = dto.content,
                writer = WriterVo(
                    memberId = dto.writer.id.toHexString(),
                    nickname = dto.writer.nickname,
                    image = dto.writer.image
                ),
                category = dto.category,
                scheduleId = dto.scheduleId,
                commentCount = dto.commentCount,
                createdAt = dto.createdAt
            )
        }
    }
}
