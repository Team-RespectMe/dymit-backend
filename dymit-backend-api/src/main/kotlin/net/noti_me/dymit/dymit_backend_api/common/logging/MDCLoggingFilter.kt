package net.noti_me.dymit.dymit_backend_api.common.logging

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MDCLoggingFilter : Filter {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain) {
        logger.debug("MDC will be set")
        val traceId = UUID.randomUUID().toString()
        MDC.put("traceId", traceId)
        try {
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}