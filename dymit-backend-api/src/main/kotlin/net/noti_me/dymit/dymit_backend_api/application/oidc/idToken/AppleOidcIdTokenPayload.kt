package net.noti_me.dymit.dymit_backend_api.application.oidc.idToken

import com.auth0.jwt.interfaces.DecodedJWT

data class AppleOidcIdTokenPayload(
    val iss: String,
    val sub: String,
    val aud: List<String>,
    val iat: Long,
    val exp: Long,
    val email: String,
    val emailVerified: Boolean,
    //val isPrivateEmail: Boolean,
    val nonceSupported: Boolean,
) {

    companion object {

        fun valueOf(decodedJWT: DecodedJWT): AppleOidcIdTokenPayload {
            return AppleOidcIdTokenPayload(
                iss = decodedJWT.issuer,
                sub = decodedJWT.subject,
                aud = decodedJWT.audience,
                iat = decodedJWT.issuedAt.time / 1000,
                exp = decodedJWT.expiresAt.time / 1000,
                email = decodedJWT.getClaim("email").asString(),
                emailVerified = decodedJWT.getClaim("email_verified").asBoolean(),
                //isPrivateEmail = decodedJWT.getClaim("is_private_email").asBoolean(),
                nonceSupported = decodedJWT.getClaim("nonce_supported").asBoolean()
            )
        }

        fun toCommonPayload(applePayload: AppleOidcIdTokenPayload): CommonOidcIdTokenPayload {
            return CommonOidcIdTokenPayload(
                iss = applePayload.iss,
                sub = applePayload.sub,
                aud = applePayload.aud,
                iat = applePayload.iat,
                exp = applePayload.exp,
                email = applePayload.email
            )
        }
    }
}