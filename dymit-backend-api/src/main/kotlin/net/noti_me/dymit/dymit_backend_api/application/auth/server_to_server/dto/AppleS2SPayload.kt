package net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.dto

import com.auth0.jwt.interfaces.DecodedJWT

data class AppleS2SPayload(
    val iss: String,
    val aud: String,
    val iat: Long,
    val jti: String,
    val events: Map<String, Any>
) {

    companion object {
        fun from(decodedJWT: DecodedJWT): AppleS2SPayload {
            val iss = decodedJWT.issuer
            val aud = decodedJWT.audience.joinToString(",")
            val iat = decodedJWT.issuedAt.time
            val jti = decodedJWT.id
            val events = decodedJWT.getClaim("events").asMap()
            return AppleS2SPayload(
                iss = iss,
                aud = aud,
                iat = iat,
                jti = jti,
                events = events
            )
        }
    }
}