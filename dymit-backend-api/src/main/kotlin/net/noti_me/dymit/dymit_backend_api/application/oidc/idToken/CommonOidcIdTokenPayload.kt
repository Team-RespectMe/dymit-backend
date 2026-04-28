package net.noti_me.dymit.dymit_backend_api.application.oidc.idToken

class CommonOidcIdTokenPayload(
    val iss: String,
    val sub: String,
    val aud: List<String>,
    val iat: Long,
    val exp: Long,
    val email: String,
    val name: String? = null,
    val profileImageUrl: String? = null
) {

    companion object {

        fun valueOf(googlePayload: GoogleOidcIdTokenPayload): CommonOidcIdTokenPayload {
            return CommonOidcIdTokenPayload(
                iss = googlePayload.iss,
                sub = googlePayload.sub,
                aud = googlePayload.aud,
                iat = googlePayload.iat,
                exp = googlePayload.exp,
                email = googlePayload.email,
                name = googlePayload.name,
                profileImageUrl = googlePayload.picture
            )
        }

        fun valueOf(applePayload: AppleOidcIdTokenPayload): CommonOidcIdTokenPayload {
            return CommonOidcIdTokenPayload(
                iss = applePayload.iss,
                sub = applePayload.sub,
                aud = applePayload.aud,
                iat = applePayload.iat,
                exp = applePayload.exp,
                name = null,
                profileImageUrl = null,
                email = applePayload.email
            )
        }
    }
}