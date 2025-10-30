package net.noti_me.dymit.dymit_backend_api.controllers

import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.AuthServiceFacade
import net.noti_me.dymit.dymit_backend_api.controllers.auth.AuthApi
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.LoginResponse
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcLoginRequest
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.RefreshTokenSubmitRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class AuthController(
    private val jwtAuthUsecase: AuthServiceFacade,
): AuthApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun oidcLogin(
        request: OidcLoginRequest
    ): LoginResponse {
        return LoginResponse.from(jwtAuthUsecase.loginByOidcToken(request.provider, request.idToken))
    }

    override fun reissueAccessToken(request: RefreshTokenSubmitRequest)
    : LoginResponse {
        return LoginResponse.from(jwtAuthUsecase.reissueAccessToken(request.refreshToken))
    }

    override fun logout(request: RefreshTokenSubmitRequest) {
        jwtAuthUsecase.logout(refreshToken = request.refreshToken)
    }
}
