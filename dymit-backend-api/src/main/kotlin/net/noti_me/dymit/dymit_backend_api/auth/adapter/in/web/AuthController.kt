package net.noti_me.dymit.dymit_backend_api.auth.adapter.`in`.web

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.auth.application.usecases.AuthServiceFacade
import net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.web.AuthApi
import net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.web.dto.LoginResponse
import net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.web.dto.OidcLoginRequest
import net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.web.dto.RefreshTokenSubmitRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.annotation.security.PermitAll

@RequestMapping("/api/v1/")
@RestController
class AuthController(
    private val jwtAuthUsecase: AuthServiceFacade,
): AuthApi {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/auth/oidc")
    @ResponseStatus(HttpStatus.CREATED)
    @PermitAll
    override fun oidcLogin(
        @RequestBody @Valid request: OidcLoginRequest
    ): LoginResponse {
        return LoginResponse.from(jwtAuthUsecase.loginByOidcToken(request.provider, request.idToken))
    }

    @PostMapping("/auth/jwt/reissue")
    @ResponseStatus(HttpStatus.OK)
    @PermitAll
    override fun reissueAccessToken(
        @RequestBody @Valid request: RefreshTokenSubmitRequest)
    : LoginResponse {
        return LoginResponse.from(jwtAuthUsecase.reissueAccessToken(request.refreshToken))
    }

    @PostMapping("/auth/jwt/blacklists")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    override fun logout(
        @RequestBody @Valid request: RefreshTokenSubmitRequest
    ) {
        jwtAuthUsecase.logout(refreshToken = request.refreshToken)
    }
}
