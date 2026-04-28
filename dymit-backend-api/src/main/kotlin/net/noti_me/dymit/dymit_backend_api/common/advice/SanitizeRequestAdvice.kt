package net.noti_me.dymit.dymit_backend_api.common.advice

import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.sanitizer.SanitizerService
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import java.lang.reflect.Type

@ControllerAdvice
class SanitizeRequestAdvice(
    private val sanitizeService: SanitizerService
): RequestBodyAdviceAdapter() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun supports(
        methodParameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>?>
    ): Boolean {
        return methodParameter.hasParameterAnnotation(Sanitize::class.java)
    }

    override fun afterBodyRead(
        body: Any,
        inputMessage: HttpInputMessage,
        parameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>?>
    ): Any {
        val sanitized = sanitizeService.sanitize(body)
            ?: body
        return super.afterBodyRead(sanitized, inputMessage, parameter, targetType, converterType)
    }
}