package net.noti_me.dymit.dymit_backend_api.application.server_notice.impl

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.GetNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class GetNoticeUseCaseImpl(
    private val serverNoticeRepository: ServerNoticeRepository
): GetNoticeUseCase {

    override fun getNotice(noticeId: String): ServerNoticeDto {
        val id = ObjectId(noticeId)
        val notice = serverNoticeRepository.findById(id)
            ?: throw NotFoundException("존재하지 않는 공지사항입니다.")

        return ServerNoticeDto.from(notice)
    }
}