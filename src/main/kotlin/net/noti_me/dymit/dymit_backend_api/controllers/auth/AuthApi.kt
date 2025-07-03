package net.noti_me.dymit.dymit_backend_api.controllers.auth

import org.springframework.web.bind.annotation.RequestBody
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcLoginRequest


interface AuthApi {

    fun oidcLogin(
        @RequestBody request: OidcLoginRequest
    )
}