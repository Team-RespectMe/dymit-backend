package net.noti_me.dymit.dymit_backend_api.study_group.domain

import kotlin.math.round

/**
 * 구성원 또는 그룹의 누적 통계 건수입니다.
 *
 * @param submittedTaskCount 제출한 과제 수
 * @param assignedTaskCount 할당된 과제 수
 * @param attendedScheduleCount 참석한 일정 수
 * @param eligibleScheduleCount 참석 대상 일정 수
 */
data class StatisticsCounts(
    val submittedTaskCount: Long = 0,
    val assignedTaskCount: Long = 0,
    val attendedScheduleCount: Long = 0,
    val eligibleScheduleCount: Long = 0
) {

    /**
     * 과제 제출률을 백분율로 반환합니다.
     *
     * @return 소수 둘째 자리까지의 제출률
     */
    fun taskSubmissionRate(): Double = percentage(submittedTaskCount, assignedTaskCount)

    /**
     * 일정 참석률을 백분율로 반환합니다.
     *
     * @return 소수 둘째 자리까지의 참석률
     */
    fun scheduleAttendanceRate(): Double = percentage(attendedScheduleCount, eligibleScheduleCount)

    operator fun plus(other: StatisticsCounts): StatisticsCounts {
        return StatisticsCounts(
            submittedTaskCount = submittedTaskCount + other.submittedTaskCount,
            assignedTaskCount = assignedTaskCount + other.assignedTaskCount,
            attendedScheduleCount = attendedScheduleCount + other.attendedScheduleCount,
            eligibleScheduleCount = eligibleScheduleCount + other.eligibleScheduleCount
        )
    }

    operator fun minus(other: StatisticsCounts): StatisticsCounts {
        return StatisticsCounts(
            submittedTaskCount = submittedTaskCount - other.submittedTaskCount,
            assignedTaskCount = assignedTaskCount - other.assignedTaskCount,
            attendedScheduleCount = attendedScheduleCount - other.attendedScheduleCount,
            eligibleScheduleCount = eligibleScheduleCount - other.eligibleScheduleCount
        )
    }

    private fun percentage(numerator: Long, denominator: Long): Double {
        if (denominator == 0L) {
            return 0.0
        }
        return round(numerator.toDouble() * 10_000.0 / denominator.toDouble()) / 100.0
    }
}
