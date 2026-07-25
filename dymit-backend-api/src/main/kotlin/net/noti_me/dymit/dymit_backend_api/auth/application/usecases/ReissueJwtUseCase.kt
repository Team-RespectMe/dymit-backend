package net.noti_me.dymit.dymit_backend_api.auth.application.usecases

import net.noti_me.dymit.dymit_backend_api.auth.application.dto.LoginResult

interface ReissueJwtUseCase {

    fun reissue(refreshToken: String): LoginResult
}

