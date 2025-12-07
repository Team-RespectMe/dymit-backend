package net.noti_me.dymit.dymit_backend_api.common.annotation

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import net.noti_me.dymit.dymit_backend_api.common.sanitizer.SanitizerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class SanitizeHandlerMethodArgumentResolver @Autowired constructor(
    private val sanitizerService: SanitizerService,
    private val objectMapper: ObjectMapper
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val clazz = parameter.parameterType
        return clazz.isAnnotationPresent(Sanitize::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)
        val json = servletRequest?.reader?.readText() ?: return null
        val arg = objectMapper.readValue(json, parameter.parameterType)
        sanitizeFields(arg)
        return arg
    }

    private fun sanitizeFields(obj: Any) {
        val clazz = obj.javaClass
        for (field in clazz.declaredFields) {
            field.isAccessible = true
            val value = field.get(obj)
            when {
                field.type == String::class.java -> {
                    if (value != null) {
                        field.set(obj, sanitizerService.sanitize(value as String))
                    }
                }
                Collection::class.java.isAssignableFrom(field.type) && value != null -> {
                    val genericType = field.genericType
                    val elementType = if (genericType is java.lang.reflect.ParameterizedType) {
                        genericType.actualTypeArguments.firstOrNull()
                    } else null
                    if (elementType == String::class.java) {
                        val sanitized = (value as Collection<*>).map {
                            if (it is String && it != null) sanitizerService.sanitize(it) else it
                        }
                        // 컬렉션 타입에 따라 대치
                        field.set(obj, when (value) {
                            is List<*> -> sanitized
                            is Set<*> -> sanitized.toSet()
                            else -> sanitized
                        })
                    } else if (elementType is Class<*> && elementType.isAnnotationPresent(Sanitize::class.java)) {
                        (value as Collection<*>).forEach {
                            if (it != null) sanitizeFields(it)
                        }
                    }
                }
                field.type.isAnnotationPresent(Sanitize::class.java) && value != null -> {
                    sanitizeFields(value)
                }
            }
        }
    }
}