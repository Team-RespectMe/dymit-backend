package net.noti_me.dymit.dymit_backend_api.server_notice.application

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.GetNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.persistence.ServerNoticeRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class GetNoticeUseCaseImpl(
    private val serverNoticeRepository: ServerNoticeRepository
): GetNoticeUseCase {

    override fun execute(noticeId: String): ServerNoticeDto {
        val id = ObjectId(noticeId)
        val notice = serverNoticeRepository.findById(id)
            ?: throw NotFoundException("존재하지 않는 공지사항입니다.")

        return ServerNoticeDto.from(notice)
    }
}
