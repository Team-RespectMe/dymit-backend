package net.noti_me.dymit.dymit_backend_api.controllers.board.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostDto

@Schema(description = "게시글 상세 응답")
class PostDetailResponse(
    @Schema(description = "게시글 ID", example = "507f1f77bcf86cd799439011")
    val id: String,
    @Schema(description = "그룹 ID", example = "507f1f77bcf86cd799439099")
    val groupId:String,
    @Schema(description = "게시판 ID", example = "507f1f77bcf86cd799439088")
    val boardId : String,
    @Schema(description = "게시글 제목", example = "첫 번째 게시글입니다.")
    val title: String,
    @Schema(description = "게시글 내용", example = "안녕하세요, 첫 번째 게시글입니다.")
    val content: String,
    val writer: WriterVo,
    @Schema(description = "게시글의 생성 시각", example = "2023-10-05T14:48:00Z")
    val createdAt: String,
) {

    companion object {
        fun from(dto: PostDto): PostDetailResponse {
            return PostDetailResponse(
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
                createdAt = dto.createdAt.toString()
            )
        }
    }
}