package net.noti_me.dymit.dymit_backend_api.application.auth.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider

interface OidcLoginUseCase {

    fun login(provider: OidcProvider, idToken: String): LoginResult
}

