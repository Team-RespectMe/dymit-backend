package net.noti_me.dymit.dymit_backend_api.application.server_notice.impl

import net.noti_me.dymit.dymit_backend_api.application.server_notice.usecases.DeleteNoticeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.server_notice.ServerNoticeRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class DeleteNoticeUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val serverNoticeRepository: ServerNoticeRepository
): DeleteNoticeUseCase {

    override fun delete(loginMember: MemberInfo, noticeId: String) {
        val id = ObjectId(noticeId)
        val notice = serverNoticeRepository.findById(id)
            ?: return

        val member = loadMemberPort.loadById(loginMember.memberId)
            ?: throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")

        if ( !member.isAdmin() ) {
            throw ForbiddenException(message = "허용되지 않는 리소스 접근입니다.")
        }
        serverNoticeRepository.delete(notice)
    }
}