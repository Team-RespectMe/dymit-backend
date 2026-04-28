package net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl

interface LogoutUseCase {

    fun logout(refreshToken: String): Boolean
}

