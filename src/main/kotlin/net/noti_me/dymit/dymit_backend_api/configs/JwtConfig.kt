package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig(
    @Value("\${jwt.secret}")
    val secret: String,
    @Value("\${jwt.access-token.expiration}")
    val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token.expiration}")
    val refreshTokenExpiration: Long,
    @Value("\${jwt.issuer}")
    val issuer: String,
    @Value("\${jwt.audience}")
    val audience: String,
) {

}