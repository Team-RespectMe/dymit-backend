package net.noti_me.dymit.dymit_backend_api.common.sanitizer

import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

@Service
class SanitizerService {

    private val logger = LoggerFactory.getLogger(javaClass)

    // 기존 정책 유지
    private val policy: PolicyFactory = HtmlPolicyBuilder()
        .allowElements(
            "b", "i", "u", "em", "strong", "strike", "s", "sup", "sub", "code", "pre", "tt", "br",
            "a", "blockquote", "p", "div", "span", "ul", "ol", "li", "dl", "dt", "dd", "hr",
            "h1", "h2", "h3", "h4", "h5", "h6", "font", "center", "table", "thead", "tbody",
            "tfoot", "tr", "td", "th", "img"
        )
        .allowAttributes("href", "rel", "target").onElements("a")
        .allowUrlProtocols("http", "https", "mailto")
        .requireRelNofollowOnLinks()
        .allowAttributes("src", "alt", "title", "width", "height").onElements("img")
        .allowUrlProtocols("http", "https", "data")
        .allowAttributes("color", "face", "size").onElements("font")
        .allowAttributes("align", "valign", "colspan", "rowspan").onElements("td", "th", "tr")
        .allowAttributes("class", "id").onElements("div", "span", "p", "table", "ul", "ol", "li")
        .toFactory()


    /**
     * 외부에서 호출하는 진입점.
     * - String → sanitizeString()
     * - Object → sanitizeFields()
     */
    fun sanitize(input: Any?): Any? {
        if (input == null) return null
        return when (input) {
            is String -> sanitizeString(input)
            else -> sanitizeFields(input)
        }
    }


    /**
     * 1) 문자열 sanitize (태그 정책 포함)
     * 2) 앞뒤 공백 제거
     */
    private fun sanitizeString(value: String): String {
        val sanitized = policy.sanitize(value).trim()
        logger.debug("Sanitized string: '$value' → '$sanitized'")
        return sanitized
    }


    /**
     * DTO / List / Map / Nested object 를 재귀적으로 탐색하여
     * 내부 String 필드를 자동 sanitize 한다.
     */
    private fun sanitizeFields(obj: Any): Any {
        val clazz = obj::class

        clazz.memberProperties.forEach { prop ->
            prop.isAccessible = true
            val value = prop.getter.call(obj)

            when (value) {
                null -> {} // skip

                is String -> {
                    val sanitized = sanitizeString(value)

                    // setter 있는 경우 (var)
                    (prop as? KMutableProperty<*>)?.setter?.call(obj, sanitized)
                        ?: forceSetImmutableField(obj, prop, sanitized) // val 이면 reflection으로 강제 수정
                }

                is Collection<*> -> value.forEach { sanitize(it) }

                is Map<*, *> -> value.values.forEach { sanitize(it) }

                else -> {
                    if (!isPrimitive(value)) {
                        sanitizeFields(value) // nested recursion
                    }
                }
            }
        }

        return obj
    }


    /**
     * primitive or safe value types 여부 검사
     * → 재귀 sanitize 수행 여부 판단용
     */
    private fun isPrimitive(value: Any): Boolean {
        return value.javaClass.kotlin.javaPrimitiveType != null ||
                value is Number || value is Boolean || value is Enum<*>
    }


    /**
     * Kotlin data class 의 val 필드를 reflection 으로 강제 변경하는 함수.
     * → setter 가 없는 경우를 처리
     */
    private fun forceSetImmutableField(target: Any, prop: KProperty1<out Any, *>, newValue: Any?) {
        val field = prop.javaField ?: return
        field.isAccessible = true
        field.set(target, newValue)
        logger.debug("Forced update immutable field '${prop.name}' → $newValue")
    }
}