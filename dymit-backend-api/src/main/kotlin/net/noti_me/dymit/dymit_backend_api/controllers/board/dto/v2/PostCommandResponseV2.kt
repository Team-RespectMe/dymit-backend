package net.noti_me.dymit.dymit_backend_api.controllers.board.dto.v2

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostDtoV2
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import java.time.LocalDateTime

/**
 * 게시글 V2 생성/수정 응답 DTO입니다.
 */
@Schema(description = "게시글 V2 생성/수정 응답 DTO")
class PostCommandResponseV2(
    val id: String,
    val title: String,
    val content: String,
    val writer: WriterVo,
    val category: PostCategory,
    val scheduleId: String?,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(dto: PostDtoV2): PostCommandResponseV2 {
            return PostCommandResponseV2(
                id = dto.id,
                title = dto.title,
                content = dto.content,
                writer = WriterVo(
                    memberId = dto.writer.id.toHexString(),
                    nickname = dto.writer.nickname,
                    image = dto.writer.image
                ),
                category = dto.category,
                scheduleId = dto.scheduleId,
                createdAt = dto.createdAt
            )
        }
    }
}
