package net.noti_me.dymit.dymit_backend_api.common.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper


class LogReportFilter(
    private val logReporter: LogReporter
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            reportRequestAndResponse(
                wrappedRequest,
                wrappedResponse
            )
            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun reportRequestAndResponse(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper
    ) {
        if ( pathMatcher.match("/api/**",request.requestURI) && response.status >= 400) {
            logReporter.send(request, response);
        }
    }
}