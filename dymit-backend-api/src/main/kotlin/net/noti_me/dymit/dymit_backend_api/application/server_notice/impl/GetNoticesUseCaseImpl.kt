package net.noti_me.dymit.dymit_backend_api.application.server_notice.impl

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.GetNoticesUseCase
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class GetNoticesUseCaseImpl(
    private val serverNoticeRepository: ServerNoticeRepository
): GetNoticesUseCase {

    override fun getNotices(cursor: String?, size: Int): List<ServerNoticeSummaryDto> {
        val cursorId: ObjectId? = cursor?.let { ObjectId(it) }
        val notices = serverNoticeRepository.findAllByCursorIdOrderByIdDesc(cursorId, size)
        return notices.map { ServerNoticeSummaryDto.from(it) }
    }
}