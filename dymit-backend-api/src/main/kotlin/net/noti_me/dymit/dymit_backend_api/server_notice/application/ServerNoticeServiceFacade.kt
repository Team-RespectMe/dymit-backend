package net.noti_me.dymit.dymit_backend_api.server_notice.application

import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.CreateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`in`.web.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.CreateNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.DeleteNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.GetNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.GetNoticesUseCase
import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.UpdateNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

@Service
class ServerNoticeServiceFacade(
    private val createNoticeUseCase: CreateNoticeUseCase,
    private val updateNoticeUseCase: UpdateNoticeUseCase,
    private val deleteNoticeUseCase: DeleteNoticeUseCase,
    private val getNoticesUseCase: GetNoticesUseCase,
    private val getNoticeUseCase: GetNoticeUseCase
) {

    fun createNotice(loginMember: MemberInfo, command: CreateServerNoticeCommand)
        = createNoticeUseCase.execute(
            loginMember = loginMember,
            command = command
        )

    fun updateNotice(loginMember: MemberInfo, command: UpdateServerNoticeCommand)
        = updateNoticeUseCase.execute(
            loginMember = loginMember,
            command = command
        )

    fun deleteNotice(loginMember: MemberInfo, noticeId: String) =
        deleteNoticeUseCase.execute(loginMember, noticeId)

    fun getNotices(cursor: String?, size: Int) = getNoticesUseCase.execute(cursor, size)

    fun getNotice(noticeId: String) = getNoticeUseCase.execute(noticeId)
}
