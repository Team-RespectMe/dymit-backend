package net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdateNoticeUseCase {

    fun execute(loginMember: MemberInfo, command: UpdateServerNoticeCommand): ServerNoticeDto
}
