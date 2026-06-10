package net.noti_me.dymit.dymit_backend_api.application.task.impl

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
}
