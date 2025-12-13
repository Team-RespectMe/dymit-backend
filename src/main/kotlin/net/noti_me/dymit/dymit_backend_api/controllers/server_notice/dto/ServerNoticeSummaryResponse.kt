package net.noti_me.dymit.dymit_backend_api.controllers.server_notice.dto

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeSummaryDto
import java.time.LocalDateTime

data class ServerNoticeSummaryResponse(
    val id: String,
    val title: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(dto: ServerNoticeSummaryDto) = ServerNoticeSummaryResponse(
            id = dto.id,
            title = dto.title,
            createdAt = dto.createdAt
        )
    }
}