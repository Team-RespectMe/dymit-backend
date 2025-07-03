package net.noti_me.dymit.dymit_backend_api.application.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import net.noti_me.dymit.dymit_backend_api.common.errors.UnauthorizedException
import net.noti_me.dymit.dymit_backend_api.configs.JwtConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.ports.persistence.LoadMemberPort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtConfig: JwtConfig,
) {

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
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(member.identifier)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .withClaim("nickname", member.nickname)
            .withArrayClaim("roles", arrayOf("ROLE_MEMBER"))
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
        return try {
            accessTokenVerifier.verify(token)
        } catch (e: JWTVerificationException) {
            throw UnauthorizedException("AE-001", "기간이 만료되거나 잘못된 액세스 토큰입니다.")
        }
    }

    fun verifyRefreshToken(token: String): DecodedJWT {
        return try {
            refreshTokenVerifier.verify(token)
        } catch (e: JWTVerificationException) {
            throw UnauthorizedException("AE-002", "기간이 만료되거나 잘못된 리프레시 토큰입니다.")
        }
    }

    fun decodeToken(token: String): DecodedJWT {
        return try {
            JWT.decode(token)
        } catch (e: JWTDecodeException) {
            throw UnauthorizedException("AE-003", "잘못된 토큰 형식입니다.")
        }
    }
}