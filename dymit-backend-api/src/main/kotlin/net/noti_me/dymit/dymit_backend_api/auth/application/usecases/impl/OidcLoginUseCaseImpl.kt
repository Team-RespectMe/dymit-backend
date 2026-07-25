package net.noti_me.dymit.dymit_backend_api.auth.application.usecases.impl

import org.springframework.stereotype.Service
import net.noti_me.dymit.dymit_backend_api.auth.application.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.LoadAuthenticationMemberPort
import net.noti_me.dymit.dymit_backend_api.auth.application.port.out.ManageAuthenticationSessionPort
import net.noti_me.dymit.dymit_backend_api.auth.application.usecases.OidcLoginUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.common.errors.*
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcProvider
import org.slf4j.LoggerFactory

@Service
class OidcLoginUseCaseImpl(
    private val oidcAuthenticationProviders: List<OidcAuthenticationProvider>,
    private val loadAuthenticationMemberPort: LoadAuthenticationMemberPort,
    private val manageAuthenticationSessionPort: ManageAuthenticationSessionPort,
    private val jwtService: JwtService
): OidcLoginUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun login(provider: OidcProvider, idToken: String): LoginResult {
        val oidcAuthenticationProvider = oidcAuthenticationProviders
            .firstOrNull { it.support(provider.name) }
            ?: throw BadRequestException(message="지원하지 않는 OIDC 프로바이더 입니다 ${provider.name}")
        val payload = oidcAuthenticationProvider.getPayload(idToken)
        logger.info("OIDC Login 요청 : ${provider.name}, sub: ${payload.sub}, email: ${payload.email}")

        val member = loadAuthenticationMemberPort.loadByOidcIdentity(
            provider = provider.name,
            subject = payload.sub
        ) ?: throw NotFoundException("존재하지 않는 회원입니다. 회원 가입이 필요합니다.")

        if ( member.isDeleted ) {
            throw UnauthorizedException("AE-002", "삭제된 회원입니다. 관리자에게 문의하세요.")
        }

        val refreshToken = jwtService.createRefreshToken(member.memberId)
        val accessToken = jwtService.createAccessToken(
            memberId = member.memberId,
            nickname = member.nickname,
            roles = member.roles
        )

        val result = LoginResult(
            memberId = member.memberId,
            accessToken = accessToken.token,
            refreshToken = refreshToken.token
        )
        manageAuthenticationSessionPort.registerRefreshToken(
            memberId = member.memberId,
            refreshToken = refreshToken.token,
            expiresAt = refreshToken.expireAt
        )
        return result
    }
}
