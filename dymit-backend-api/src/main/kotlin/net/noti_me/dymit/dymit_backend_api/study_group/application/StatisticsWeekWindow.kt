package net.noti_me.dymit.dymit_backend_api.study_group.application

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * 한국 시간 기준으로 닫힌 주의 배타적 상한을 계산합니다.
 */
object StatisticsWeekWindow {
    private val koreaZone = ZoneId.of("Asia/Seoul")

    /**
     * 조회 시각이 속한 주의 월요일 00:00을 반환합니다.
     */
    fun cutoffAt(observedAt: Instant): Instant {
        val date = observedAt.atZone(koreaZone).toLocalDate()
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(koreaZone)
            .toInstant()
    }

    /**
     * 지정 시각 다음의 첫 한국 시간 월요일 00:00을 반환합니다.
     */
    fun firstWeekEndAfter(instant: Instant): Instant {
        return instant.atZone(koreaZone).toLocalDate()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .atStartOfDay(koreaZone)
            .toInstant()
    }

    /**
     * 시작 시각 다음의 첫 주 종료부터 상한까지를 오래된 순서로 반환합니다.
     */
    fun weekEnds(joinedAt: Instant, cutoff: Instant): List<Instant> {
        val result = mutableListOf<Instant>()
        var cursor = firstWeekEndAfter(joinedAt)
        while (cursor <= cutoff) {
            result.add(cursor)
            cursor = cursor.plusSeconds(WEEK_SECONDS)
        }
        return result
    }

    private const val WEEK_SECONDS = 7L * 24L * 60L * 60L
}
