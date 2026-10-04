package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.study_group.application.StatisticsWeekWindow
import java.time.Instant

internal class StatisticsWeekWindowTest : BehaviorSpec({
    Given("2026년 10월 3일의 조회") {
        Then("조회 주가 아니라 전주 종료를 한국 시간 기준 상한으로 사용한다") {
            StatisticsWeekWindow.cutoffAt(Instant.parse("2026-10-03T01:00:00Z")) shouldBe
                Instant.parse("2026-09-27T15:00:00Z")
        }
    }
    Given("한국 시간 월요일 자정의 직전과 직후") {
        Then("상한은 자정에 한 번만 다음 주로 전진한다") {
            val monday = Instant.parse("2026-09-27T15:00:00Z")
            StatisticsWeekWindow.cutoffAt(monday.minusNanos(1)) shouldBe monday.minusSeconds(604800)
            StatisticsWeekWindow.cutoffAt(monday) shouldBe monday
            StatisticsWeekWindow.cutoffAt(monday.plusNanos(1)) shouldBe monday
        }
    }
    Given("여러 주 동안 통계를 조회하지 않은 구성원") {
        Then("가입 이후 닫힌 주를 모두 오래된 순서로 생성한다") {
            StatisticsWeekWindow.weekEnds(
                Instant.parse("2026-09-07T00:00:00Z"),
                Instant.parse("2026-09-27T15:00:00Z")
            ) shouldBe listOf(
                Instant.parse("2026-09-13T15:00:00Z"),
                Instant.parse("2026-09-20T15:00:00Z"),
                Instant.parse("2026-09-27T15:00:00Z")
            )
        }
    }
    Given("이번 주에 가입하여 아직 닫힌 주가 없는 구성원") {
        Then("미래 주간 스냅샷을 생성하지 않는다") {
            StatisticsWeekWindow.weekEnds(
                Instant.parse("2026-09-28T00:00:00Z"),
                Instant.parse("2026-09-27T15:00:00Z")
            ) shouldBe emptyList()
        }
    }
})
