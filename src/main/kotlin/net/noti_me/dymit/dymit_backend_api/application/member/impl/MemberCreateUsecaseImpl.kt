package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.MemberCreateUsecase
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.SaveMemberPort
import org.springframework.stereotype.Service

@Service
class MemberCreateUsecaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val oidcAuthenticationProviders: List<OidcAuthenticationProvider>
) : MemberCreateUsecase {

    override fun createMember(command: MemberCreateCommand): MemberCreateResult {
        val oidcAuthenticationProvider = oidcAuthenticationProviders
            .firstOrNull { it.support(command.oidcProvider.name) }
            ?: throw IllegalArgumentException("지원하지 않는 OIDC 프로바이더 입니다 ${command.oidcProvider.name}")

        val payload = oidcAuthenticationProvider.getPayload(command.idToken)
        loadMemberPort.loadByOidcIdentity(OidcIdentity(provider = command.oidcProvider.name, subject = payload.sub))
            ?.let{ throw ConflictException("이미 회원가입이 된 계정입니다.") }

        var member = Member(
            nickname = command.nickname,
            oidcIdentities = mutableSetOf(OidcIdentity(
                provider = command.oidcProvider.name,
                subject = payload.sub
            )),
        )

        member = saveMemberPort.persist(member)

        return MemberCreateResult.from(
            member = MemberDto.fromEntity(member),
            loginResult = LoginResult(
                accessToken = payload.sub,
                refreshToken = payload.sub
            )
        )
    }
}