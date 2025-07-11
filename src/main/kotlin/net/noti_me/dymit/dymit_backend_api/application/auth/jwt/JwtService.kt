package net.noti_me.dymit.dymit_backend_api.application.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.common.errors.UnauthorizedException
import net.noti_me.dymit.dymit_backend_api.configs.JwtConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.LoadMemberPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtConfig: JwtConfig,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val algorithm = Algorithm.HMAC256(
        jwtConfig.secret.toByteArray(Charsets.UTF_8)
    )

    private val accessTokenVerifier = JWT.require(algorithm)
        .withIssuer(jwtConfig.issuer)
        .withAudience(jwtConfig.audience)
        .build()


    private val refreshTokenVerifier = JWT.require(algorithm)
        .withIssuer(jwtConfig.issuer)
        .withAudience(jwtConfig.audience)
        .build()

    fun createAccessToken(member: Member): String {
        val now = Instant.now()
        val expiresAt = now.plusMillis(jwtConfig.accessTokenExpiration)
        logger.debug("expiry: ${jwtConfig.accessTokenExpiration}")
        logger.debug("Creating access token for member: ${member.identifier}, expires at: $expiresAt")
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(member.identifier)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .withClaim("nickname", member.nickname)
            .withArrayClaim("roles", arrayOf(MemberRole.ROLE_MEMBER.name))
            .sign(algorithm)
    }

    fun createRefreshToken(member: Member): String {
        val now = Instant.now()
        val expiresAt = now.plusMillis(jwtConfig.refreshTokenExpiration)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(member.identifier)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun verifyAccessToken(token: String): DecodedJWT {
        return accessTokenVerifier.verify(token)
    }

    fun verifyRefreshToken(token: String): DecodedJWT {
        return refreshTokenVerifier.verify(token)
    }

    fun decodeToken(token: String): DecodedJWT {
        return try {
            JWT.decode(token)
        } catch (e: JWTDecodeException) {
            throw UnauthorizedException("AE-003", "잘못된 토큰 형식입니다.")
        }
    }
}