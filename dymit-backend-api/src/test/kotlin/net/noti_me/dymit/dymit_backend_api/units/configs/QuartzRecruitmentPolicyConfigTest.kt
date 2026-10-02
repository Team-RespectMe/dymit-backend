package net.noti_me.dymit.dymit_backend_api.units.configs

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.configs.QuartzConfig
import org.quartz.CronTrigger
import org.springframework.context.annotation.Bean

internal class QuartzRecruitmentPolicyConfigTest : BehaviorSpec({

    Given("모집글 자동 삭제 정책이 폐기된 Quartz 설정") {
        Then("통계와 알림 빈만 등록하고 모집글 자동 삭제 작업과 트리거를 등록하지 않는다") {
            val beanMethods = QuartzConfig::class.java.declaredMethods
                .filter { it.isAnnotationPresent(Bean::class.java) }
                .map { it.name }

            beanMethods.shouldContainExactlyInAnyOrder(
                "memberDailyStatisticsJobDetail", "memberDailyStatisticsTrigger",
                "studyGroupDailyStatisticsJobDetail", "studyGroupDailyStatisticsTrigger",
                "studyScheduleDailyStatisticsJobDetail", "studyScheduleDailyStatisticsTrigger",
                "taskDailyStatisticsJobDetail", "taskDailyStatisticsTrigger",
                "dailyStatisticsReportJobDetail", "dailyStatisticsReportTrigger",
                "dailyScheduleReminderJobDetail", "triggerOn9AMUTC9",
                "hourlyScheduleReminderJobDetail", "triggerEveryHour"
            )
        }

        Then("일간 및 시간별 일정 알림 스케줄은 유지한다") {
            val config = QuartzConfig()
            val dailyJob = config.dailyScheduleReminderJobDetail()
            val hourlyJob = config.hourlyScheduleReminderJobDetail()
            val dailyTrigger = config.triggerOn9AMUTC9(dailyJob) as CronTrigger
            val hourlyTrigger = config.triggerEveryHour(hourlyJob) as CronTrigger

            dailyTrigger.jobKey shouldBe dailyJob.key
            dailyTrigger.cronExpression shouldBe "0 0 9 * * ?"
            dailyTrigger.timeZone.id shouldBe "Asia/Seoul"
            hourlyTrigger.jobKey shouldBe hourlyJob.key
            hourlyTrigger.cronExpression shouldBe "0 0 * * * ?"
            hourlyTrigger.timeZone.id shouldBe "Asia/Seoul"
        }
    }
})
