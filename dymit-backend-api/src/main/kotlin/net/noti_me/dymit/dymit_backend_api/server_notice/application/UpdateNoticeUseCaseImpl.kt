package net.noti_me.dymit.dymit_backend_api.server_notice.application

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.UpdateNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.ServerNoticeMemberPort
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.persistence.ServerNoticeRepository
import org.springframework.stereotype.Service

@Service
class UpdateNoticeUseCaseImpl(
    private val serverNoticeMemberPort: ServerNoticeMemberPort,
    private val serverNoticeRepository: ServerNoticeRepository
): UpdateNoticeUseCase {

    override fun execute(loginMember: MemberInfo, command: UpdateServerNoticeCommand)
    : ServerNoticeDto {
        val member = serverNoticeMemberPort.loadById(loginMember.memberId)
            ?: throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")
        val notice = serverNoticeRepository.findById(command.noticeId)
            ?: throw NotFoundException(message = "존재하지 않는 공지사항입니다.")

        val requester = member.toRequester()
        notice.updateContent(requester = requester, command.content)
        notice.updateTitle(requester = requester, command.title)
        notice.updateCategory(requester = requester, command.category)
        val updatedNotice = serverNoticeRepository.save(notice)
        return ServerNoticeDto.from(updatedNotice)
    }
}
