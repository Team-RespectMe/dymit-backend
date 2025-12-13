package net.noti_me.dymit.dymit_backend_api.application.server_notice.dto

import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import org.bson.types.ObjectId
import java.time.LocalDateTime

class ServerNoticeDto(
    val id: ObjectId,
    val writer: Writer,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(notice: ServerNotice): ServerNoticeDto {
//            println("notice : $notice")
            return ServerNoticeDto(
                id = notice.id!!,
                writer = notice.writer,
                title = notice.title,
                content = notice.content,
                createdAt = notice.createdAt!!,
                updatedAt = notice.updatedAt!!
            )
        }
    }
}