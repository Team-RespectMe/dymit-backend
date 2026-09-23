package net.noti_me.dymit.dymit_backend_api.configs

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Jackson이 오프셋 없는 기존 시각 문자열을 한국 표준시 기준으로 Instant로 변환합니다.
 */
internal class LegacyInstantDeserializer : JsonDeserializer<Instant>() {

    /**
     * JSON 문자열을 오프셋 규칙에 맞는 Instant로 변환합니다.
     *
     * 오프셋이 있으면 입력된 오프셋을 그대로 사용하고, 오프셋이 없으면 Asia/Seoul을 사용합니다.
     */
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): Instant {
        if (!p.hasToken(JsonToken.VALUE_STRING)) {
            @Suppress("UNCHECKED_CAST")
            return ctxt.handleUnexpectedToken(Instant::class.java, p) as Instant
        }

        val value = p.text
        return try {
            parse(value)
        } catch (exception: DateTimeParseException) {
            throw ctxt.weirdStringException(
                value,
                Instant::class.java,
                "유효한 ISO-8601 시각 형식이 아닙니다."
            )
        }
    }

    private fun parse(value: String): Instant = try {
        OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
    } catch (_: DateTimeParseException) {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant()
    }
}
