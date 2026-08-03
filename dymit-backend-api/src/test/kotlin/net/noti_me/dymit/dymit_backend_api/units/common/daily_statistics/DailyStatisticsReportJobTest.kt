package net.noti_me.dymit.dymit_backend_api.units.common.daily_statistics

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsReportDocument
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsReportJob
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindow
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.MemberDailyStatisticsReport
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.StudyGroupDailyStatisticsReport
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.StudyScheduleDailyStatisticsReport
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.TaskDailyStatisticsReport
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DailyStatisticsReportFormatter
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordMessageDto
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordWebhookTransport
import net.noti_me.dymit.dymit_backend_api.configs.DiscordConfig
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

internal class DailyStatisticsReportJobTest : BehaviorSpec({
    val date = LocalDate.of(2026, 7, 28)
    val window = DailyStatisticsWindow(date, date.atTime(4, 0), date.plusDays(1).atTime(4, 0))

    beforeTest {
        mockkObject(DailyStatisticsWindowCalculator)
        every { DailyStatisticsWindowCalculator.calculate() } returns window
    }
    afterTest { unmockkObject(DailyStatisticsWindowCalculator) }

    Given("a completed previous Korean business-day document") {
        val mongoTemplate = mockk<MongoTemplate>()
        val config = mockk<DiscordConfig>()
        val formatter = mockk<DailyStatisticsReportFormatter>()
        val transport = mockk<DiscordWebhookTransport>()
        val document = reportDocument(date)
        val message = DiscordMessageDto(content = "report")
        every { mongoTemplate.findOne(any<Query>(), DailyStatisticsReportDocument::class.java, "daily_stats") } returns document
        every { config.getDailyStatisticsUrl() } returns "https://discord.example/daily"
        every { formatter.format(document) } returns message
        every { transport.send("https://discord.example/daily", message) } returns Mono.empty()

        When("the 05:00 report job runs") {
            DailyStatisticsReportJob(mongoTemplate, config, formatter, transport).execute(null)

            Then("it loads that date and makes exactly one daily-channel send attempt") {
                val query = slot<Query>()
                verify(exactly = 1) {
                    mongoTemplate.findOne(
                        capture(query),
                        DailyStatisticsReportDocument::class.java,
                        "daily_stats"
                    )
                }
                query.captured.queryObject.containsKey("statisticDate") shouldBe true
                verify(exactly = 1) { formatter.format(document) }
                verify(exactly = 1) { transport.send("https://discord.example/daily", message) }
            }
        }
    }

    Given("a missing statistics document") {
        val mongoTemplate = mockk<MongoTemplate>()
        val formatter = mockk<DailyStatisticsReportFormatter>()
        val transport = mockk<DiscordWebhookTransport>()
        every { mongoTemplate.findOne(any<Query>(), DailyStatisticsReportDocument::class.java, "daily_stats") } returns null

        When("the report job runs") {
            DailyStatisticsReportJob(mongoTemplate, mockk(), formatter, transport).execute(null)

            Then("it returns without formatting or sending") {
                verify(exactly = 0) { formatter.format(any()) }
                verify(exactly = 0) { transport.send(any(), any()) }
            }
        }
    }

    Given("a Discord transport failure") {
        val mongoTemplate = mockk<MongoTemplate>()
        val config = mockk<DiscordConfig>()
        val formatter = mockk<DailyStatisticsReportFormatter>()
        val transport = mockk<DiscordWebhookTransport>()
        val document = reportDocument(date)
        val message = DiscordMessageDto(content = "report")
        every { mongoTemplate.findOne(any<Query>(), DailyStatisticsReportDocument::class.java, "daily_stats") } returns document
        every { config.getDailyStatisticsUrl() } returns "https://discord.example/daily"
        every { formatter.format(document) } returns message
        every { transport.send(any(), any()) } returns Mono.error(IllegalStateException("offline"))

        When("the report job runs") {
            Then("it does not throw or retry delivery") {
                shouldNotThrowAny {
                    DailyStatisticsReportJob(mongoTemplate, config, formatter, transport).execute(null)
                }
                verify(exactly = 1) { transport.send("https://discord.example/daily", message) }
            }
        }
    }

    Given("a report document") {
        When("it is formatted") {
            Then("the embed includes every business value and timestamp without an id") {
                val embed = DailyStatisticsReportFormatter().format(reportDocument(date)).embeds.single()

                embed.title shouldContain "2026-07-28"
                listOf(
                    "2026-07-28 04:00:00", "2026-07-29 04:00:00", "가입: 1", "탈퇴: 2", "방문: 3",
                    "스터디 그룹", "생성: 4", "스터디 일정\n- 생성: 5", "참여 회원(중복 제외): 6", "생성: 7", "제출: 8",
                    "2026-07-29 05:01:02", "2026-07-29 05:03:04"
                ).forEach { embed.description shouldContain it }
                embed.description shouldNotContain "_id"
            }
        }
    }
}) {
    companion object {
        private fun reportDocument(date: LocalDate): DailyStatisticsReportDocument = DailyStatisticsReportDocument(
            statisticDate = date,
            windowStart = date.atTime(4, 0),
            windowEnd = date.plusDays(1).atTime(4, 0),
            member = MemberDailyStatisticsReport(1, 2, 3),
            studyGroup = StudyGroupDailyStatisticsReport(4),
            studySchedule = StudyScheduleDailyStatisticsReport(5, 6),
            task = TaskDailyStatisticsReport(7, 8),
            createdAt = LocalDateTime.of(2026, 7, 29, 5, 1, 2),
            updatedAt = LocalDateTime.of(2026, 7, 29, 5, 3, 4)
        )
    }
}
