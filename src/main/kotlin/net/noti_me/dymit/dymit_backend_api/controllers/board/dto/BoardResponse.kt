package net.noti_me.dymit.dymit_backend_api.controllers.board.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardDto
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission

@Schema(description = "Board Response DTO")
class BoardResponse(
    @Schema(description = "게시판 ID", example = "507f1f77bcf86cd799439011")
    val id: String,
    @Schema(description = "스터디 그룹 ID", example = "507f1f77bcf86cd799439012")
    val groupId: String,
    @Schema(description = "게시판 이름", example = "공지 사항")
    val name: String,
    @Schema(description = "게시판 생성일", example = "2026-10-01T12:00:00Z")
    val createdAt: String,
    @Schema(description = "그룹 멤버 권한에 따른 게시판 허용 기능 목록", example = "[\"OWNER\" : [\"CREATE_POST\", \"EDIT_POST\", \"DELETE_POST\"], \"MEMBER\": [\"CREATE_POST\"]]")
    val permissions: List<BoardPermission>
) {

    companion object {
        fun from(dto: BoardDto): BoardResponse {
            return BoardResponse(
                id = dto.id,
                groupId = dto.groupId,
                name = dto.name,
                createdAt = dto.createdAt.toString(),
                permissions = dto.permissions
            )
        }
    }
}