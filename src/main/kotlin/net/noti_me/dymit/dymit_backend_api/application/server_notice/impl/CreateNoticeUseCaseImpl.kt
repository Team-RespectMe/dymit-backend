package net.noti_me.dymit.dymit_backend_api.application.server_notice.impl

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.CreateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.CreateNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import org.springframework.stereotype.Service

@Service
class CreateNoticeUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val serverNoticeRepository: ServerNoticeRepository,
): CreateNoticeUseCase {

    override fun create(loginMember: MemberInfo, command: CreateServerNoticeCommand): ServerNoticeDto {
        val member = loadMemberPort.loadById(loginMember.memberId)
            ?: throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")

        val notice = ServerNotice.create(
            writer = member,
            title = command.title,
            content = command.content
        )
        val savedNotice = serverNoticeRepository.save(notice)
        return ServerNoticeDto.from(savedNotice)
    }
}