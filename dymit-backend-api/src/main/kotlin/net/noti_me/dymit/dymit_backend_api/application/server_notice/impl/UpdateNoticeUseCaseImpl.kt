package net.noti_me.dymit.dymit_backend_api.application.server_notice.impl

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.UpdateNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import org.springframework.stereotype.Service

@Service
class UpdateNoticeUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val serverNoticeRepository: ServerNoticeRepository
): UpdateNoticeUseCase {

    override fun update(loginMember: MemberInfo, command: UpdateServerNoticeCommand)
    : ServerNoticeDto {
        val member = loadMemberPort.loadById(loginMember.memberId)
            ?: throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")
        val notice = serverNoticeRepository.findById(command.noticeId)
            ?: throw NotFoundException(message = "존재하지 않는 공지사항입니다.")

        notice.updateContent(requester = member,command.content)
        notice.updateTitle(requester = member,command.title)
        notice.updateCategory(requester = member,command.category)
        val updatedNotice = serverNoticeRepository.save(notice)
        return ServerNoticeDto.from(updatedNotice)
    }
}
