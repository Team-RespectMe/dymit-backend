package net.noti_me.dymit.dymit_backend_api.common.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JwtAuthenticationFilter
 * JWT 인증 필터로, 요청에 포함된 JWT 토큰을 검증하고 인증 정보를 설정한다.
 * 이 필터는 OncePerRequestFilter를 상속받아 매 요청마다 한 번만 실행된다.
 */
class JwtAuthenticationFilter(
    private val authenticationManager: AuthenticationManager
) : OncePerRequestFilter() {

//    val regex = Regex("^Bearer\\s+(.*)$")
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.debug("JwtAuthenticationFilter doFilterInternal called")
        val token = extractTokenFromHeader(request)
        logger.debug("Extracted token: $token")
        if ( token == null ) {
            logger.debug("No JWT token found in request header")
            filterChain.doFilter(request, response)
            return;
        }

        // 토큰이 존재하면 JwtAuthenticationToken 생성
        val jwtAuthenticationToken = JwtAuthenticationToken(principal = null,  credentials = token)
        // 인증 매니저를 사용하여 인증 시도
        val authentication = authenticationManager.authenticate(jwtAuthenticationToken)
        // 인증이 성공하면 SecurityContext에 인증 정보 설정
        if (authentication.isAuthenticated) {
            SecurityContextHolder.getContext().authentication = authentication
        }
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromHeader(request: HttpServletRequest): String? {
        // "Authorization: Bearer <token>" 헤더에서 토큰을 추출하는 로직
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }
        return null
    }
}