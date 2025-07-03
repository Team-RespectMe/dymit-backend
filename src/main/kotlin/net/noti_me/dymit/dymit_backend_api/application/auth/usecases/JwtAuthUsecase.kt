package net.noti_me.dymit.dymit_backend_api.application.auth.usecases

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider

interface JwtAuthUsecase {

    fun login(provider: OidcProvider, idToken: String): LoginResult

    fun reissueAccessToken(refreshToken: String): LoginResult

    fun logout(refreshToken: String): Boolean
}
