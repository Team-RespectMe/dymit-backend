package net.noti_me.dymit.dymit_backend_api.application.oidc.idToken

import com.auth0.jwt.interfaces.DecodedJWT

class KakaoOidcIdTokenPayload(
    val iss: String,
    val aud: List<String>,
    val sub: String,
    val iat: Long,
    val exp: Long,
    val nonce: String,
    val auth_time: Long
) {

    companion object {
        fun valueOf(decodedJWT: DecodedJWT): KakaoOidcIdTokenPayload {
            return KakaoOidcIdTokenPayload(
                iss = decodedJWT.issuer,
                aud = decodedJWT.audience,
                sub = decodedJWT.subject,
                iat = decodedJWT.issuedAt.time / 1000,
                exp = decodedJWT.expiresAt.time / 1000,
                nonce = decodedJWT.claims["nonce"]?.asString() ?: "",
                auth_time = decodedJWT.claims["auth_time"]?.asLong() ?: 0L
            )
        }
    }

    fun toCommonPayload(): CommonOidcIdTokenPayload {
        return CommonOidcIdTokenPayload(
            iss = this.iss,
            sub = this.sub,
            aud = this.aud,
            iat = this.iat,
            exp = this.exp,
            email = "kakao:${sub}@kakao.com",
            name = "KakaoUser_$sub",
            profileImageUrl = null
        )
    }
}

