package net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.web.dto

import jakarta.validation.constraints.*
import net.noti_me.dymit.dymit_backend_api.common.security.oidc.OidcProvider

data class OidcLoginRequest(
    val provider: OidcProvider,
    @field: NotEmpty(message = "ID Token은 필수입니다.")
    val idToken: String
) {

}
