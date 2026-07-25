package net.noti_me.dymit.dymit_backend_api.auth.application.usecases

import net.noti_me.dymit.dymit_backend_api.auth.application.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.auth.application.usecases.LogoutUseCase
import net.noti_me.dymit.dymit_backend_api.auth.application.usecases.OidcLoginUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcProvider
import org.springframework.stereotype.Service

@Service
class AuthServiceFacade(
    private val oidcLoginUseCase: OidcLoginUseCase,
    private val reissueJwtUseCase: ReissueJwtUseCase,
    private val logoutUseCase: LogoutUseCase
) {

    fun loginByOidcToken(provider: OidcProvider, idToken: String): LoginResult {
        return oidcLoginUseCase.login(
            provider = provider,
            idToken = idToken
        )
    }

    fun reissueAccessToken(refreshToken: String): LoginResult {
        return reissueJwtUseCase.reissue(refreshToken)
    }

    fun logout(refreshToken: String): Boolean {
        return logoutUseCase.logout(refreshToken)
    }
}
