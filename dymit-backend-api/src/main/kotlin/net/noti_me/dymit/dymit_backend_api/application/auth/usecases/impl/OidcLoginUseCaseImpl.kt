package net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl

import org.springframework.stereotype.Service
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.auth.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.errors.*
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.slf4j.LoggerFactory

@Service
class OidcLoginUseCaseImpl(
    private val oidcAuthenticationProviders: List<OidcAuthenticationProvider>,
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val jwtService: JwtService
): OidcLoginUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun login(provider: OidcProvider, idToken: String): LoginResult {
        val oidcAuthenticationProvider = oidcAuthenticationProviders
            .firstOrNull { it.support(provider.name) }
            ?: throw BadRequestException(message="지원하지 않는 OIDC 프로바이더 입니다 ${provider.name}")
        val payload = oidcAuthenticationProvider.getPayload(idToken)
        logger.info("OIDC Login 요청 : ${provider.name}, sub: ${payload.sub}, email: ${payload.email}")

        val member = loadMemberPort.loadByOidcIdentity(
            OidcIdentity(
                provider = provider.name,
                subject = payload.sub
            )
        ) ?: throw NotFoundException("존재하지 않는 회원입니다. 회원 가입이 필요합니다.")

        if ( member.isDeleted ) {
            throw UnauthorizedException("AE-002", "삭제된 회원입니다. 관리자에게 문의하세요.")
        }

        val refreshToken = jwtService.createRefreshToken(member)
        val accessToken = jwtService.createAccessToken(member)

        val result = LoginResult(
            memberId = member.identifier,
            accessToken = accessToken.token,
            refreshToken = refreshToken.token
        )
        member.addRefreshToken(refreshToken.token, refreshToken.expireAt)
        saveMemberPort.persist(member)
        return result
    }
}

