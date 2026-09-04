package net.noti_me.dymit.dymit_backend_api.units.common.daily_statistics

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import java.time.Instant
import java.time.LocalDate

internal class DailyStatisticsWindowCalculatorTest : BehaviorSpec({
    Given("a UTC instant at the Korean business boundary") {
        When("the daily window is calculated") {
            Then("it uses the previous Korean date with an inclusive/exclusive 04:00 window") {
                val window = DailyStatisticsWindowCalculator.calculate(Instant.parse("2026-07-29T19:00:00Z"))

                window.statisticDate shouldBe LocalDate.of(2026, 7, 29)
                window.windowStart shouldBe Instant.parse("2026-07-28T19:00:00Z")
                window.windowEnd shouldBe Instant.parse("2026-07-29T19:00:00Z")
            }
        }
    }

    Given("the same instant regardless of the JVM default time zone") {
        When("the daily window is calculated") {
            Then("the Korean window remains unchanged") {
                val instant = Instant.parse("2026-07-29T19:00:00Z")

                DailyStatisticsWindowCalculator.calculate(instant) shouldBe
                    DailyStatisticsWindowCalculator.calculate(instant)
            }
        }
    }
})
