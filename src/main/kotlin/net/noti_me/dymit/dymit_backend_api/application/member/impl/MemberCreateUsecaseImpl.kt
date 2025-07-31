package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl.JwtAuthService
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.application.member.usecases.MemberCreateUsecase
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MemberCreateUsecaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val oidcAuthenticationProviders: List<OidcAuthenticationProvider>,
    private val jwtAuthService: JwtAuthService
) : MemberCreateUsecase {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun createMember(command: MemberCreateCommand): MemberCreateResult {
        val oidcAuthenticationProvider = oidcAuthenticationProviders
            .firstOrNull { it.support(command.oidcProvider.name) }
            ?: throw IllegalArgumentException("지원하지 않는 OIDC 프로바이더 입니다 ${command.oidcProvider.name}")

        val payload = oidcAuthenticationProvider.getPayload(command.idToken)

        logger.debug("회원가입 요청: ${command.oidcProvider.name}, sub: ${payload.sub}, email: ${payload.email}")
        loadMemberPort.loadByOidcIdentity(OidcIdentity(provider = command.oidcProvider.name, subject = payload.sub))
            ?.let{ throw ConflictException(message= "이미 회원가입이 된 계정입니다.") }

        if ( loadMemberPort.existsByNickname(command.nickname) ) {
            throw ConflictException("CONFLICT", "이미 사용 중인 닉네임입니다.")
        }

        var member = Member(
            nickname = command.nickname,
            oidcIdentities = mutableSetOf(OidcIdentity(
                provider = command.oidcProvider.name,
                subject = payload.sub,
                email = payload.email
            )),
        )

        member = saveMemberPort.persist(member)

        return MemberCreateResult.from(
            member = MemberDto.fromEntity(member),
            loginResult = jwtAuthService.login(
                provider = command.oidcProvider,
                idToken = command.idToken
            )
        )
    }

    override fun checkNickname(nickname: String) {
        val exist = loadMemberPort.existsByNickname(nickname)
        if (exist) {
            throw ConflictException("CONFLICT", "이미 사용 중인 닉네임입니다.")
        }
    }
}