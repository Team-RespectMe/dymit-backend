package net.noti_me.dymit.dymit_backend_api.task.application

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * 과제 마감 시각 정규화를 담당하는 유틸리티입니다.
 */
object TaskExpireAtNormalizer {
    private val koreaZoneId: ZoneId = ZoneId.of("Asia/Seoul")
    private val utcZoneId: ZoneId = ZoneOffset.UTC

    /**
     * 사후 과제 마감 시각을 UTC 기준 하루 종료 시각으로 정규화합니다.
     *
     * @param requestedExpireAt 요청된 마감 시각
     * @return 정규화된 마감 시각
     */
    fun normalizePostExpireAt(requestedExpireAt: LocalDateTime): LocalDateTime {
        val endOfDayKst = requestedExpireAt.toLocalDate().atTime(23, 59, 59)
        return endOfDayKst
            .atZone(koreaZoneId)
            .withZoneSameInstant(utcZoneId)
            .toLocalDateTime()
    }

    /**
     * UTC 기준 현재 시각을 반환합니다.
     *
     * @return UTC 기준 현재 시각
     */
    fun currentUtcDateTime(): LocalDateTime = LocalDateTime.now(utcZoneId)

    /**
     * UTC 기준 저장 값을 KST 기준 시각으로 변환합니다.
     *
     * @param utcDateTime UTC 기준 시각
     * @return KST 기준 시각
     */
    fun toKst(utcDateTime: LocalDateTime): LocalDateTime = utcDateTime
        .atZone(utcZoneId)
        .withZoneSameInstant(koreaZoneId)
        .toLocalDateTime()

    /**
     * 저장된 마감 시각이 현재 기준으로 만료되었는지 확인합니다.
     *
     * @param expireAtUtc UTC 기준 저장 마감 시각
     * @return 만료 여부
     */
    fun isExpired(expireAtUtc: LocalDateTime): Boolean = toKst(expireAtUtc).isBefore(toKst(currentUtcDateTime()))
}
