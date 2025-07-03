package net.noti_me.dymit.dymit_backend_api.supports

import net.noti_me.dymit.dymit_backend_api.application.auth.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.configs.JwtConfig

//class JwtSupport {

fun createJwtConfig(
    issuer: String = "test-issuer",
    audience: String = "test-audience",
    secret:String = "test-secret",
    accessTokenExpiration: Long = 3600000L,
    refreshTokenExpiration: Long = 604800000L, // 7 days
): JwtConfig {
    return JwtConfig(
        issuer = issuer,
        audience = audience,
        accessTokenExpiration = accessTokenExpiration,
        refreshTokenExpiration = refreshTokenExpiration,
        secret = secret
    )
}

fun createJwtService(jwtConfig: JwtConfig): JwtService {
    return JwtService(jwtConfig)
}
//}