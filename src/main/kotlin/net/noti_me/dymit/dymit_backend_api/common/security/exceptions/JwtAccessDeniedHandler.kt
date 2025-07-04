package net.noti_me.dymit.dymit_backend_api.common.security.exceptions

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.noti_me.dymit.dymit_backend_api.common.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class JwtAccessDeniedHandler(
    private val om: ObjectMapper
) : AccessDeniedHandler {

    private val DEFAULT_ACCESS_DENIED_JSON: String by lazy {
        om.writeValueAsString(DEFAULT_ACCESS_DENIED)
    }

    companion object {
        private val DEFAULT_ACCESS_DENIED = ErrorResponse.of(
            status = HttpStatus.FORBIDDEN,
            code = "ACCESS_DENIED",
            message = "요청을 수행하는데 필요한 권한이 부족합니다."
        )
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(DEFAULT_ACCESS_DENIED_JSON)
        response.writer.flush()
        response.writer.close()
    }
}