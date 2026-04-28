package net.noti_me.dymit.dymit_backend_api.controllers.board.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardDto
import java.time.LocalDateTime

@Schema(description = "게시판 목록 조회 응답 DTO")
class BoardListItem(
    val id: String,
    val groupId: String,
    val name: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(dto: BoardDto): BoardListItem {
            return BoardListItem(
                id = dto.id,
                groupId = dto.groupId,
                name = dto.name,
                createdAt = dto.createdAt
            )
        }
    }
}