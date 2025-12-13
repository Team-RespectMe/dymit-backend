package net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice

import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
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