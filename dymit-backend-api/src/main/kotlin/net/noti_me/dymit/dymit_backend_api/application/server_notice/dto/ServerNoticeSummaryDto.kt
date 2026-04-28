package net.noti_me.dymit.dymit_backend_api.application.server_notice.dto

import net.noti_me.dymit.dymit_backend_api.domain.server_notice.Link
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import java.time.LocalDateTime

class ServerNoticeSummaryDto(
    val id: String,
    val category: String,
    val title: String,
    val link : Link? = null,
    val createdAt: LocalDateTime,
) {

    companion object {
        fun from(entity: ServerNotice): ServerNoticeSummaryDto {
            return ServerNoticeSummaryDto(
                id = entity.identifier,
                category = entity.category,
                title = entity.title,
                link = entity.link,
                createdAt = entity.createdAt ?: LocalDateTime.now()
            )
        }
    }
}
