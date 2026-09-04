package net.noti_me.dymit.dymit_backend_api.units.common.daily_statistics

import com.mongodb.client.result.UpdateResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.member.adapter.`out`.daily_statistics.MongoMemberDailyStatisticsAdapter
import net.noti_me.dymit.dymit_backend_api.member.application.port.`out`.daily_statistics.MemberDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.`out`.daily_statistics.MongoStudyGroupDailyStatisticsAdapter
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`out`.daily_statistics.StudyGroupDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.adapter.`out`.daily_statistics.MongoStudyScheduleDailyStatisticsAdapter
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`out`.daily_statistics.StudyScheduleDailyStatisticsDto
import net.noti_me.dymit.dymit_backend_api.task.adapter.`out`.daily_statistics.MongoTaskDailyStatisticsAdapter
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.daily_statistics.TaskDailyStatisticsDto
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId

internal class DailyStatisticsDuplicateKeyRetryTest : BehaviorSpec({
    val date = LocalDate.of(2026, 7, 28)
    val start = date.atTime(4, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant()
    val end = date.plusDays(1).atTime(4, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant()

    Given("a concurrent first daily-statistics insert") {
        When("the member upsert gets a duplicate key") {
            Then("it retries exactly once with only its owned normal fields and reports non-created") {
                val mongoTemplate = duplicateKeyMongoTemplate()
                val retry = slot<Update>()
                every { mongoTemplate.updateFirst(any<Query>(), capture(retry), "daily_stats") } returns UpdateResult.acknowledged(1, 1, null)

                MongoMemberDailyStatisticsAdapter(mongoTemplate)
                    .upsert(date, start, end, MemberDailyStatisticsDto(1, 2, 3)) shouldBe false

                verify(exactly = 1) { mongoTemplate.upsert(any<Query>(), any<Update>(), "daily_stats") }
                verify(exactly = 1) { mongoTemplate.updateFirst(any<Query>(), any<Update>(), "daily_stats") }
                retry.captured.updateObject["\$setOnInsert"] shouldBe null
                retry.captured.updateObject["\$set"].toString().contains("member.joinedCount") shouldBe true
            }
        }

        When("the group upsert gets a duplicate key") {
            Then("it retries once without insert-only fields and reports non-created") {
                val mongoTemplate = duplicateKeyMongoTemplate()
                val retry = slot<Update>()
                every { mongoTemplate.updateFirst(any<Query>(), capture(retry), "daily_stats") } returns UpdateResult.acknowledged(1, 1, null)

                MongoStudyGroupDailyStatisticsAdapter(mongoTemplate)
                    .upsert(date, start, end, StudyGroupDailyStatisticsDto(4)) shouldBe false

                verify(exactly = 1) { mongoTemplate.updateFirst(any<Query>(), any<Update>(), "daily_stats") }
                retry.captured.updateObject["\$setOnInsert"] shouldBe null
                retry.captured.updateObject["\$set"].toString().contains("studyGroup.createdCount") shouldBe true
            }
        }

        When("the schedule upsert gets a duplicate key") {
            Then("it retries once with its two owned fields and reports non-created") {
                val mongoTemplate = duplicateKeyMongoTemplate()
                val retry = slot<Update>()
                every { mongoTemplate.updateFirst(any<Query>(), capture(retry), "daily_stats") } returns UpdateResult.acknowledged(1, 1, null)

                MongoStudyScheduleDailyStatisticsAdapter(mongoTemplate)
                    .upsert(date, start, end, StudyScheduleDailyStatisticsDto(5, 6)) shouldBe false

                verify(exactly = 1) { mongoTemplate.updateFirst(any<Query>(), any<Update>(), "daily_stats") }
                retry.captured.updateObject["\$setOnInsert"] shouldBe null
                retry.captured.updateObject["\$set"].toString().contains("studySchedule.participantMemberCount") shouldBe true
            }
        }

        When("the task upsert gets a duplicate key") {
            Then("it retries once with its two owned fields and reports non-created") {
                val mongoTemplate = duplicateKeyMongoTemplate()
                val retry = slot<Update>()
                every { mongoTemplate.updateFirst(any<Query>(), capture(retry), "daily_stats") } returns UpdateResult.acknowledged(1, 1, null)

                MongoTaskDailyStatisticsAdapter(mongoTemplate)
                    .upsert(date, start, end, TaskDailyStatisticsDto(7, 8)) shouldBe false

                verify(exactly = 1) { mongoTemplate.updateFirst(any<Query>(), any<Update>(), "daily_stats") }
                retry.captured.updateObject["\$setOnInsert"] shouldBe null
                retry.captured.updateObject["\$set"].toString().contains("task.submittedCount") shouldBe true
            }
        }
    }
}) {
    companion object {
        private fun duplicateKeyMongoTemplate(): MongoTemplate {
            return mockk<MongoTemplate>().also { mongoTemplate ->
                every { mongoTemplate.upsert(any<Query>(), any<Update>(), "daily_stats") } throws DuplicateKeyException("concurrent insert")
            }
        }
    }
}
