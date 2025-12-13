package net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface UpdateNoticeUseCase {

    fun update(loginMember: MemberInfo, command: UpdateServerNoticeCommand): ServerNoticeDto
}