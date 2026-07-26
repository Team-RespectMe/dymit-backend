package net.noti_me.dymit.dymit_backend_api.server_notice.application

import net.noti_me.dymit.dymit_backend_api.server_notice.application.usecase.DeleteNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.ServerNoticeMemberPort
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.persistence.ServerNoticeRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class DeleteNoticeUseCaseImpl(
    private val serverNoticeMemberPort: ServerNoticeMemberPort,
    private val serverNoticeRepository: ServerNoticeRepository
): DeleteNoticeUseCase {

    override fun execute(loginMember: MemberInfo, noticeId: String) {
        val id = ObjectId(noticeId)
        val notice = serverNoticeRepository.findById(id)
            ?: return

        val member = serverNoticeMemberPort.loadById(loginMember.memberId)
            ?: throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")

        if ( !member.admin ) {
            throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")
        }
        serverNoticeRepository.delete(notice)
    }
}
