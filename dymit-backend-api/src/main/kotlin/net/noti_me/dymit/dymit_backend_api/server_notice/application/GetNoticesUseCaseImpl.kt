package net.noti_me.dymit.dymit_backend_api.server_notice.application

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeSummaryDto
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.GetNoticesUseCase
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.persistence.ServerNoticeRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class GetNoticesUseCaseImpl(
    private val serverNoticeRepository: ServerNoticeRepository
): GetNoticesUseCase {

    override fun execute(cursor: String?, size: Int): List<ServerNoticeSummaryDto> {
        val cursorId: ObjectId? = cursor?.let { ObjectId(it) }
        val notices = serverNoticeRepository.findAllByCursorIdOrderByIdDesc(cursorId, size)
        return notices.map { ServerNoticeSummaryDto.from(it) }
    }
}
