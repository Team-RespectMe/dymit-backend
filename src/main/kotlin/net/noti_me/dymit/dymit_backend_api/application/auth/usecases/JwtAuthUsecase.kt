package net.noti_me.dymit.dymit_backend_api.application.auth.usecases

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult

interface JwtAuthUsecase {

    fun reissueAccessToken(refreshToken: String): LoginResult

    fun logout(refreshToken: String): Boolean
}
