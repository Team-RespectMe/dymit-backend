package net.noti_me.dymit.dymit_backend_api.controllers

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.auth.usecases.JwtAuthUsecase
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcLoginRequest
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val jwtAuthUsecase: JwtAuthUsecase
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/oidc")
    fun oidcLogin(
        @RequestBody request: OidcLoginRequest
    ): LoginResult {
        logger.debug("OIDC login request received for provider: ${request.provider} with idToken: ${request.idToken}")
        return jwtAuthUsecase.login(request.provider, request.idToken)
    }
}