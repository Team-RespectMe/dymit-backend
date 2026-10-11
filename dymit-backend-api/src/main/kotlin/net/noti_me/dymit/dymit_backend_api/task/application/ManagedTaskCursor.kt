package net.noti_me.dymit.dymit_backend_api.task.application

import org.bson.types.ObjectId
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.util.Base64

/**
 * 관리 과제 목록의 조회 날짜와 정렬 위치를 보존하는 복합 커서입니다.
 *
 * @param date 최초 조회의 한국 날짜
 * @param expireAt 마지막 과제의 마감 시각
 * @param taskId 마지막 과제 ID
 */
data class ManagedTaskCursor(
    val date: LocalDate,
    val expireAt: Instant,
    val taskId: ObjectId
) {

    /** URL 전달용 문자열로 인코딩합니다. */
    fun encode(): String {
        val payload = listOf(date, expireAt.toEpochMilli(), taskId.toHexString()).joinToString(DELIMITER)
        val encoded = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        return "$VERSION.$encoded"
    }

    companion object {

        /** URL 전달용 문자열을 복합 커서로 디코딩합니다. */
        fun decode(value: String): ManagedTaskCursor {
            return try {
                decodeValue(value)
            } catch (exception: RuntimeException) {
                throw IllegalArgumentException("올바르지 않은 관리 과제 커서입니다.", exception)
            }
        }

        private fun decodeValue(value: String): ManagedTaskCursor {
            require(value.startsWith("$VERSION."))
            val encoded = value.substringAfter('.', missingDelimiterValue = "")
            require(encoded.isNotEmpty())
            val parts = String(
                Base64.getUrlDecoder().decode(encoded),
                StandardCharsets.UTF_8
            ).split(DELIMITER)
            require(parts.size == PART_COUNT)
            require(ObjectId.isValid(parts[2]))
            return ManagedTaskCursor(
                date = LocalDate.parse(parts[0]),
                expireAt = Instant.ofEpochMilli(parts[1].toLong()),
                taskId = ObjectId(parts[2])
            )
        }

        private const val VERSION = "v1"
        private const val DELIMITER = ":"
        private const val PART_COUNT = 3
    }
}
