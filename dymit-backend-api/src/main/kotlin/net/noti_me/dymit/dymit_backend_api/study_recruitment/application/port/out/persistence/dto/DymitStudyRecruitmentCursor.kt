package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto

import org.bson.types.ObjectId
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

/**
 * Dymit 스터디 모집글 목록의 복합 커서입니다.
 *
 * @property bumpAt 커서 모집글의 조회 당시 끌어올리기 시각
 * @property recruitmentId 커서 모집글 ObjectId
 * @property hasStoredBumpAt Mongo 문서에 끌어올리기 시각이 저장되어 있는지 여부
 */
data class DymitStudyRecruitmentCursor(
    val bumpAt: Instant,
    val recruitmentId: ObjectId,
    val hasStoredBumpAt: Boolean = true
) {

    /**
     * 커서를 URL 전달용 문자열로 인코딩합니다.
     *
     * @return 버전 정보를 포함한 URL 안전 커서 문자열
     */
    fun encode(): String {
        val payload = listOf(
            bumpAt.epochSecond,
            bumpAt.nano,
            recruitmentId.toHexString(),
            if ( hasStoredBumpAt ) 1 else 0
        ).joinToString(DELIMITER)
        val encodedPayload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        return "$VERSION.$encodedPayload"
    }

    companion object {

        /**
         * URL 전달용 문자열을 복합 커서로 디코딩합니다.
         *
         * @param value 인코딩된 커서 문자열
         * @return 디코딩된 복합 커서
         * @throws IllegalArgumentException 커서 형식이 올바르지 않은 경우
         */
        fun decode(value: String): DymitStudyRecruitmentCursor {
            return try {
                decodeValue(value)
            } catch ( exception: RuntimeException ) {
                throw IllegalArgumentException("올바르지 않은 Dymit 모집글 커서입니다.", exception)
            }
        }

        private fun decodeValue(value: String): DymitStudyRecruitmentCursor {
            require(value.startsWith("$VERSION."))
            val encodedPayload = value.substringAfter('.', missingDelimiterValue = "")
            require(encodedPayload.isNotEmpty())
            val parts = String(
                Base64.getUrlDecoder().decode(encodedPayload),
                StandardCharsets.UTF_8
            ).split(DELIMITER)
            require(parts.size == PART_COUNT)
            require(ObjectId.isValid(parts[2]))
            require(parts[3] == "0" || parts[3] == "1")

            return DymitStudyRecruitmentCursor(
                bumpAt = Instant.ofEpochSecond(parts[0].toLong(), parts[1].toLong()),
                recruitmentId = ObjectId(parts[2]),
                hasStoredBumpAt = parts[3] == "1"
            )
        }

        private const val VERSION = "v1"
        private const val DELIMITER = ":"
        private const val PART_COUNT = 4
    }
}
