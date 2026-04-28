package net.noti_me.dymit.dymit_backend_api.common.advice

import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.common.response.Envelop
import org.slf4j.MDC
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@ControllerAdvice
class EnvelopPatternAdvice() : ResponseBodyAdvice<Any> {

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        return BaseResponse::class.java.isAssignableFrom(returnType.parameterType)
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        val responseStatus = returnType.getMethodAnnotation(ResponseStatus::class.java)
        var status = HttpStatus.OK
        if (responseStatus != null) {
            status = responseStatus.value
        }
        response.setStatusCode(status)

        return Envelop(
            status = status.value(),
            data = body,
            traceId = MDC.get("traceId")
        )
    }
}