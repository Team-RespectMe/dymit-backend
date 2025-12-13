package net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.controllers.board.dto.WriterVo
import java.time.LocalDateTime

data class ServerNoticeResponse(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from( dto: ServerNoticeDto ) = ServerNoticeResponse(
            id = dto.id.toString(),
            title = dto.title,
            content = dto.content,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
}