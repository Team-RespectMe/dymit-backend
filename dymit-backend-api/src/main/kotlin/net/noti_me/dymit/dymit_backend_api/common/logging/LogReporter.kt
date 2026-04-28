package net.noti_me.dymit.dymit_backend_api.common.logging

import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

interface LogReporter {

    fun send(request: ContentCachingRequestWrapper, response: ContentCachingResponseWrapper)
}