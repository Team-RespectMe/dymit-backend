package net.noti_me.dymit.dymit_backend_api.units.study_group.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts

internal class StatisticsCountsTest : BehaviorSpec({
    Given("대상 건수가 서로 다른 두 구성원의 통계") {
        Then("그룹 비율은 구성원 비율의 평균 대신 전체 건수로 계산한다") {
            val first = StatisticsCounts(1, 1, 1, 1)
            val second = StatisticsCounts(1, 9, 1, 9)
            val group = first + second
            group.submittedTaskCount shouldBe 2L
            group.assignedTaskCount shouldBe 10L
            group.taskSubmissionRate() shouldBe 20.0
            group.scheduleAttendanceRate() shouldBe 20.0
        }
    }
    Given("과제와 일정 대상이 없는 가입 구간") {
        Then("비율은 NaN이나 무한대 없이 0을 반환한다") {
            StatisticsCounts().taskSubmissionRate() shouldBe 0.0
            StatisticsCounts().scheduleAttendanceRate() shouldBe 0.0
        }
    }
    Given("분모가 서로 다른 참석과 제출 실적") {
        Then("각 비율을 소수 둘째 자리까지 독립적으로 계산한다") {
            val counts = StatisticsCounts(2, 3, 1, 6)
            counts.taskSubmissionRate() shouldBe 66.67
            counts.scheduleAttendanceRate() shouldBe 16.67
        }
    }
})
