package net.noti_me.dymit.dymit_backend_api.application.server_notice

import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.CreateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.ServerNoticeDto
import net.noti_me.dymit.dymit_backend_api.application.server_notice.dto.UpdateServerNoticeCommand
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.CreateNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.DeleteNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.GetNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.GetNoticesUseCase
import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.UpdateNoticeUseCase
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
        = createNoticeUseCase.create(
            loginMember = loginMember,
            command = command
        )

    fun updateNotice(loginMember: MemberInfo, command: UpdateServerNoticeCommand)
        = updateNoticeUseCase.update(
            loginMember = loginMember,
            command = command
        )

    fun deleteNotice(loginMember: MemberInfo, noticeId: String) =
        deleteNoticeUseCase.delete(loginMember, noticeId)

    fun getNotices(cursor: String?, size: Int) = getNoticesUseCase.getNotices(cursor, size)

    fun getNotice(noticeId: String) = getNoticeUseCase.getNotice(noticeId)
}