package net.noti_me.dymit.dymit_backend_api.server_notice.application

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.CreateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.CreateNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.server_notice.domain.ServerNotice
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.ServerNoticeMemberPort
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.persistence.ServerNoticeRepository
import org.springframework.stereotype.Service

@Service
class CreateNoticeUseCaseImpl(
    private val serverNoticeMemberPort: ServerNoticeMemberPort,
    private val serverNoticeRepository: ServerNoticeRepository,
): CreateNoticeUseCase {

    override fun execute(loginMember: MemberInfo, command: CreateServerNoticeCommand): ServerNoticeDto {
        val member = serverNoticeMemberPort.loadById(loginMember.memberId)
            ?: throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")

        val notice = ServerNotice.create(
            writer = member.toRequester(),
            title = command.title,
            content = command.content,
            category = command.category
        )
        val savedNotice = serverNoticeRepository.save(notice)
        return ServerNoticeDto.from(savedNotice)
    }
}
