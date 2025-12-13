package net.noti_me.dymit.dymit_backend_api.application.server_notice.dto

import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import java.time.LocalDateTime

class ServerNoticeSummaryDto(
    val id: String,
    val title: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(entity: ServerNotice): ServerNoticeSummaryDto {
            return ServerNoticeSummaryDto(
                id = entity.identifier,
                title = entity.title,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}