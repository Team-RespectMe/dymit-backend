package net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.JwtAuthUsecase
import net.noti_me.dymit.dymit_backend_api.application.oidc.OidcAuthenticationProvider
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.LoadMemberPort
import org.springframework.stereotype.Service

@Service
class JwtAuthService(
    private val oidcAuthenticationProviders: List<OidcAuthenticationProvider>,
    private val loadMemberPort: LoadMemberPort
): JwtAuthUsecase {

    override fun login(provider: OidcProvider, idToken: String): LoginResult {
        val oidcAuthenticationProvider = oidcAuthenticationProviders
            .firstOrNull { it.support(provider.name) }
            ?: throw BadRequestException("지원하지 않는 OIDC 프로바이더 입니다 ${provider.name}")
        val payload = oidcAuthenticationProvider.getPayload(idToken)

        val member = loadMemberPort.loadByOidcIdentity(
            OidcIdentity(
                provider = provider.name,
                subject = payload.sub
            )
        ) ?: throw NotFoundException("존재하지 않는 회원입니다. 회원 가입이 필요합니다.")

        return LoginResult(
            accessToken = payload.sub,
            refreshToken = payload.sub
        )
    }

    override fun reissueAccessToken(refreshToken: String): LoginResult {
        TODO("Not yet implemented")
    }

    override fun logout(refreshToken: String): Boolean {
        TODO("Not yet implemented")
    }
}