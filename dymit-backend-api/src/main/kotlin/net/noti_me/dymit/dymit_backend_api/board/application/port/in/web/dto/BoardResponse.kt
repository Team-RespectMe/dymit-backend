package net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.board.application.port.`in`.v1.dto.BoardDto
import net.noti_me.dymit.dymit_backend_api.board.domain.BoardPermission
import java.time.Instant

@Schema(description = "Board Response DTO")
class BoardResponse(
    @Schema(description = "게시판 ID", example = "507f1f77bcf86cd799439011")
    val id: String,
    @Schema(description = "스터디 그룹 ID", example = "507f1f77bcf86cd799439012")
    val groupId: String,
    @Schema(description = "게시판 이름", example = "공지 사항")
    val name: String,
    @Schema(description = "게시판 생성일", example = "2026-10-01T12:00:00Z")
    val createdAt: Instant,
    @Schema(description = "그룹 멤버 권한에 따른 게시판 허용 기능 목록", example = "[\"OWNER\" : [\"CREATE_POST\", \"EDIT_POST\", \"DELETE_POST\"], \"MEMBER\": [\"CREATE_POST\"]]")
    val permissions: List<BoardPermission>
) {

    companion object {
        fun from(dto: BoardDto): BoardResponse {
            return BoardResponse(
                id = dto.id,
                groupId = dto.groupId,
                name = dto.name,
                createdAt = dto.createdAt,
                permissions = dto.permissions
            )
        }
    }
}
