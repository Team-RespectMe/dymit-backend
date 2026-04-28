package net.noti_me.dymit.dymit_backend_api.application.auth.dto

import com.auth0.jwt.interfaces.DecodedJWT
import java.io.Serializable

class JwtClaims(
    val memberId: String,
    val nickname: String,
    val roles: List<String>
): Serializable {

    companion object {
        fun from(decodedJWT: DecodedJWT): JwtClaims {
            val memberId = decodedJWT.subject
            val nickname = decodedJWT.getClaim("nickname").asString()
            val roles = decodedJWT.getClaim("roles").asList(String::class.java) ?: emptyList()
            return JwtClaims(
                memberId = memberId,
                nickname = nickname,
                roles = roles
            )
        }
    }
}