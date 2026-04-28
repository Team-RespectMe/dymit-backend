package net.noti_me.dymit.dymit_backend_api.common.security.exceptions

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.noti_me.dymit.dymit_backend_api.common.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class JwtEntrypointUnauthorizedHandler(
    private val om: ObjectMapper
) : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val JWT_VERIFICATION_EXCEPTION_JSON: String by lazy {
        om.writeValueAsString(JWT_VERIFICATION_EXCEPTION)
    }

    private val DEFAULT_UNAUTHORIZED_EXCEPTION_JSON: String by lazy {
        om.writeValueAsString(DEFAULT_UNAUTHORIZED_EXCEPTION)
    }

    companion object {
        private val JWT_VERIFICATION_EXCEPTION = ErrorResponse(
            code = "JWT-001",
            message = "JWT 토큰 검증에 실패했습니다. 이미 만료되었거나 변조된 토큰입니다.",
            status = 401
        )


        private val DEFAULT_UNAUTHORIZED_EXCEPTION = ErrorResponse(
            code = "UNAUTHORIZED",
            message = "인증되지 않은 사용자입니다. 인증이 필요한 엔드포인트를 접근하는 경우 인증 헤더에 JWT 토큰을 포함시켜주세요.",
            status = 401
        )
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        val body = if ( isJwtException(authException) ) {
            JWT_VERIFICATION_EXCEPTION_JSON
        } else {
            DEFAULT_UNAUTHORIZED_EXCEPTION_JSON
        }

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(body)
        response.writer.flush()
        response.writer.close()
    }

    private fun isJwtException(exception: AuthenticationException?): Boolean {
        return exception != null &&
                exception.message != null &&
                exception.message!!.contains("JWTVerificationException")
    }
}