package net.noti_me.dymit.dymit_backend_api.units.configs

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.configs.QuartzConfig
import org.quartz.CronTrigger

internal class QuartzDailyStatisticsConfigTest : BehaviorSpec({
    Given("the daily statistics Quartz configuration") {
        When("its collection and report triggers are created") {
            Then("it has four distinct 04:00 collectors and one 05:00 report in Asia/Seoul") {
                val config = QuartzConfig()
                val triggers = listOf(
                    config.memberDailyStatisticsTrigger(config.memberDailyStatisticsJobDetail()),
                    config.studyGroupDailyStatisticsTrigger(config.studyGroupDailyStatisticsJobDetail()),
                    config.studyScheduleDailyStatisticsTrigger(config.studyScheduleDailyStatisticsJobDetail()),
                    config.taskDailyStatisticsTrigger(config.taskDailyStatisticsJobDetail())
                ).map { it as CronTrigger }

                triggers.map { it.key.name }.shouldContainExactlyInAnyOrder(
                    "memberDailyStatisticsTrigger",
                    "studyGroupDailyStatisticsTrigger",
                    "studyScheduleDailyStatisticsTrigger",
                    "taskDailyStatisticsTrigger"
                )
                triggers.forEach {
                    it.cronExpression shouldBe "0 0 4 * * ?"
                    it.timeZone.id shouldBe "Asia/Seoul"
                }

                val reportTrigger = config.dailyStatisticsReportTrigger(
                    config.dailyStatisticsReportJobDetail()
                ) as CronTrigger
                reportTrigger.key.name shouldBe "dailyStatisticsReportTrigger"
                reportTrigger.cronExpression shouldBe "0 0 5 * * ?"
                reportTrigger.timeZone.id shouldBe "Asia/Seoul"
            }
        }
    }
})
