package net.noti_me.dymit.dymit_backend_api.auth.application.usecases

interface LogoutUseCase {

    fun logout(refreshToken: String): Boolean
}
