package net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.persistence

import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNotice
import org.bson.types.ObjectId

interface ServerNoticeRepository {

    fun findById(noticeId: ObjectId): ServerNotice?

    fun save(serverNotice: ServerNotice): ServerNotice

    fun deleteById(noticeId: ObjectId): Unit

    fun delete(serverNotice: ServerNotice): Unit

    fun findAllByCursorIdOrderByIdDesc(
        cursorId: ObjectId?,
        pageSize: Int
    ): List<ServerNotice>
}