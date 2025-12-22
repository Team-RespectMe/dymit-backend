package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.usecases.UpdateOidcIdentityUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.springframework.stereotype.Service

/**
 * OIDC Identity 업데이트 유스케이스 구현체
 * - 기존 OIDC Identity를 가진 멤버를 조회하고, 새로운 OIDC Identity로 업데이트합니다.
 * - 멤버가 존재하지 않으면 NotFoundException을 던집니다.
 * - 업데이트된 멤버 정보를 저장합니다.
 * - 이 유즈케이스는 Oidc Provider와의 머신 투 머신 통신에서만 사용됩니다.
 *   따라서 MemberServiceFacade에 포함시켜서는 안됩니다.
 */
@Service
class UpdateOidcIdentityUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort
): UpdateOidcIdentityUseCase {

    override fun update(newOidcIdentity: OidcIdentity) {
        val member = loadMemberPort.loadByOidcIdentity(oidcIdentity = newOidcIdentity)
            ?: throw NotFoundException(message = "Member not exists, Maybe leaved or deleted member. oidcIdentity: $newOidcIdentity")
        member.updateOidcIdentity(newOidcIdentity)
        saveMemberPort.update(member)
    }
}