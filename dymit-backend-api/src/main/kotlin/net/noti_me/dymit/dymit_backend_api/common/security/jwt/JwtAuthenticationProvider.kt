package net.noti_me.dymit.dymit_backend_api.common.security.jwt

import com.auth0.jwt.exceptions.JWTVerificationException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtAuthenticationProvider(
    private val jwtService: JwtService,
): AuthenticationProvider {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun supports(authentication: Class<*>?): Boolean {
        return JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
    }

    override fun authenticate(authentication: Authentication?): Authentication? {
        authentication as JwtAuthenticationToken

        try {
            // Access Token을 검증하고 DecodedJWT를 가져옵니다.
            val tokenString = authentication.credentials as String
            logger.debug("Attempting to verify JWT access token: $tokenString")
            val jwt = try {
                jwtService.verifyAccessToken(authentication.credentials as String)
            } catch(e: JWTVerificationException) {
                logger.error("JWT verification failed: ${e.message}")
                throw BadCredentialsException("JWTVerificationException", e)
            }

            // "roles" 클레임을 문자열 리스트로 변환합니다.
            val roles = jwt.roles.map { roleName ->
                when (roleName) {
                    "ROLE_MEMBER", "ROLE_ADMIN" -> roleName
                    else -> throw BadCredentialsException("JWT-002")
                }
            }

            // 문자열 리스트를 GrantedAuthority 리스트로 변환합니다.
            val authorities = roles.map { SimpleGrantedAuthority(it) }

            // 인증된 사용자 정보를 담을 Principal 객체를 생성합니다.
            val principal = MemberInfo.of(
                memberId = jwt.memberId,
                nickname = jwt.nickname,
                roles = roles
            )
            // 인증 완료를 나타내는 새로운 AuthenticationToken을 생성하여 반환합니다.
            return JwtAuthenticationToken(principal, null, authorities)
        } catch (e: JWTVerificationException) {
            // 토큰 검증 실패 시 (예: 만료, 서명 불일치) 예외를 던집니다.
            throw BadCredentialsException("JWTVerificationException", e)
        }
    }
}
