package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.usecases.ForceDeleteMemberUseCase
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * OIDC Provider 에서 발행한 이벤트로 멤버를 강제 삭제하는 유스케이스 구현체
 * MemberServiceFacade에는 포함되지 않음
 */
@Service
class ForceDeleteMemberUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val eventPublisher: ApplicationEventPublisher
): ForceDeleteMemberUseCase {

    override fun forceDelete(memberId: String) {
        val member = loadMemberPort.loadById(memberId)
            ?: return
        member.leaveService()
        saveMemberPort.delete(member)
    }
}