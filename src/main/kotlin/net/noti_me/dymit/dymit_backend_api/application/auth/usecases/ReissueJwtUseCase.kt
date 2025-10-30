package net.noti_me.dymit.dymit_backend_api.application.auth.usecases

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult

interface ReissueJwtUseCase {

    fun reissue(refreshToken: String): LoginResult
}

