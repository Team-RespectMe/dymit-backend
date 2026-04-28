package net.noti_me.dymit.dymit_backend_api.controllers.auth.dto

import jakarta.validation.constraints.*

data class OidcLoginRequest(
    val provider: OidcProvider,
    @field: NotEmpty(message = "ID Token은 필수입니다.")
    val idToken: String
) {

}