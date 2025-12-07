package net.noti_me.dymit.dymit_backend_api.common.sanitizer
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import org.slf4j.LoggerFactory
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import org.springframework.stereotype.Service
import java.lang.reflect.Field
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import java.time.*
import java.util.*

@Service
class SanitizerService {

    private val logger = LoggerFactory.getLogger(javaClass)

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


    // 🔥 엔트리 포인트: String이면 바로 sanitize, 객체면 annotation 검사
    fun sanitize(input: Any?): Any? {
        if (input == null) return null

        if (input is String) {
            return sanitizeString(input)
        }

        val clazz = input::class
        if (!clazz.hasAnnotation<Sanitize>()) {
            return input
        }

        sanitizeRecursive(input)
        return input
    }


    private fun sanitizeRecursive(obj: Any) {
        val clazz = obj::class

        clazz.memberProperties.forEach { prop ->
            prop.isAccessible = true
            val value = prop.getter.call(obj) ?: return@forEach

            val newValue: Any? = when {
                value is String -> sanitizeString(value)

                value is Map<*, *> -> sanitizeMap(value)

                value is Iterable<*> -> sanitizeIterable(value)

                value is Array<*> -> sanitizeArray(value)

                isSanitizableObject(value) -> {
                    sanitize(value)
                }

                else -> null
            }

            if (newValue != null && newValue !== value) {
                replaceValue(obj, prop, newValue)
            }
        }
    }


    // ---------- String Sanitizer ----------
    private fun sanitizeString(value: String): String {
        val result = policy.sanitize(value).trim()
        logger.debug("[SANITIZE] '$value' -> '$result'")
        return result
    }


    // ---------- Collection 처리 ----------
    private fun sanitizeIterable(src: Iterable<*>): Iterable<*> {
        val result = mutableListOf<Any?>()
        src.forEach { item -> result.add(sanitize(item)) }
        return result
    }

    private fun sanitizeArray(src: Array<*>): Array<*> {
        return Array(src.size) { i -> sanitize(src[i]) }
    }

    // ---------- Map 처리 (key/value 모두 sanitize) ----------
    private fun sanitizeMap(src: Map<*, *>): Map<Any?, Any?> {
        val result = LinkedHashMap<Any?, Any?>()
        src.forEach { (k, v) ->
            val newKey = sanitize(k)
            val newVal = sanitize(v)
            result[newKey] = newVal
        }
        return result
    }


    private fun replaceValue(target: Any, prop: KProperty1<out Any, *>, newValue: Any?) {
        (prop as? KMutableProperty<*>)?.setter?.call(target, newValue)
            ?: run {
                val field = prop.javaField ?: return
                if (!field.tryMakeAccessible()) return
                field.set(target, newValue)
            }
    }


    // ---------- Object Filter ----------
    private fun isSanitizableObject(value: Any): Boolean {
        return when (value) {
            is Number, is Boolean, is Enum<*>, is UUID,
            is LocalDate, is LocalDateTime, is Instant -> false

            else -> value::class.hasAnnotation<Sanitize>()
        }
    }


    // ---------- 안전한 reflection wrapper ----------
    private fun Field.tryMakeAccessible(): Boolean {
        return try {
            isAccessible = true
            true
        } catch (_: Exception) {
            false
        }
    }
}