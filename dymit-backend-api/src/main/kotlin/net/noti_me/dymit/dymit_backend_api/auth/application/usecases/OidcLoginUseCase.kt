package net.noti_me.dymit.dymit_backend_api.auth.application.usecases

import net.noti_me.dymit.dymit_backend_api.auth.application.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcProvider

interface OidcLoginUseCase {

    fun login(provider: OidcProvider, idToken: String): LoginResult
}
