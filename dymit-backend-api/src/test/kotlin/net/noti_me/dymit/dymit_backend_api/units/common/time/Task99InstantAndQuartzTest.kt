package net.noti_me.dymit.dymit_backend_api.units.common.time

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsReportDocument
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindow
import net.noti_me.dymit.dymit_backend_api.common.daily_statistics.DailyStatisticsWindowCalculator
import net.noti_me.dymit.dymit_backend_api.configs.QuartzConfig
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.CreateStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentSummaryResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.StudyRecruitmentResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.UpdateStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`out`.member.dto.DymitStudyRecruitmentMemberDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`out`.persistence.dto.DymitStudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto.StudyScheduleCommandRequest
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.web.dto.StudyScheduleResponse
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleDetailDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto.TaskCommandRequest
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto.TaskResponse
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto.TaskSubmissionCommentResponse
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto.TaskSubmissionResponse
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto.TaskUpdateRequest
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import org.bson.types.ObjectId
import org.quartz.CronExpression
import org.quartz.CronTrigger
import java.lang.reflect.Field
import java.time.Instant
import java.util.Date

internal class Task99InstantAndQuartzTest : BehaviorSpec({

    Given("the affected domain, DTO, request, and response models") {
        Then("every timestamp field is represented by Instant") {
            val fieldsByType = linkedMapOf(
                BaseAggregateRoot::class.java to setOf("createdAt", "updatedAt"),
                DymitStudyRecruitment::class.java to setOf("recruitmentStart", "recruitmentEnd", "bumpAt"),
                StudyRecruitment::class.java to setOf("createdAt", "updatedAt"),
                StudySchedule::class.java to setOf("scheduleAt", "createdAt", "updatedAt"),
                Task::class.java to setOf("expireAt", "createdAt", "updatedAt"),
                DailyStatisticsWindow::class.java to setOf("windowStart", "windowEnd"),
                DailyStatisticsReportDocument::class.java to setOf("windowStart", "windowEnd", "createdAt", "updatedAt"),
                DymitStudyRecruitmentPersistenceDto::class.java to setOf(
                    "recruitmentStart", "recruitmentEnd", "createdAt", "updatedAt", "bumpAt"
                ),
                DymitStudyRecruitmentDto::class.java to setOf(
                    "recruitmentStart", "recruitmentEnd", "createdAt", "updatedAt", "bumpAt"
                ),
                StudyScheduleDetailDto::class.java to setOf("scheduleAt", "createdAt", "updatedAt"),
                TaskDto::class.java to setOf("expireAt"),
                TaskSubmissionDto::class.java to setOf("createdAt"),
                TaskSubmissionCommentDto::class.java to setOf("createdAt"),
                CreateStudyRecruitmentRequest::class.java to setOf("recruitmentStart", "recruitmentEnd"),
                UpdateStudyRecruitmentRequest::class.java to setOf("recruitmentStart", "recruitmentEnd"),
                DymitStudyRecruitmentResponse::class.java to setOf(
                    "recruitmentStart", "recruitmentEnd", "createdAt", "updatedAt", "bumpAt"
                ),
                DymitStudyRecruitmentSummaryResponse::class.java to setOf("createdAt"),
                StudyRecruitmentResponse::class.java to setOf("createdAt", "updatedAt"),
                StudyScheduleCommandRequest::class.java to setOf("scheduleAt"),
                StudyScheduleResponse::class.java to setOf("scheduleAt"),
                TaskCommandRequest::class.java to setOf("expireAt"),
                TaskUpdateRequest::class.java to setOf("expireAt"),
                TaskResponse::class.java to setOf("expireAt"),
                TaskSubmissionResponse::class.java to setOf("createdAt"),
                TaskSubmissionCommentResponse::class.java to setOf("createdAt")
            )

            fieldsByType.forEach { (type, fieldNames) ->
                fieldNames.forEach { fieldName ->
                    findField(type, fieldName).type shouldBe Instant::class.java
                }
            }
        }
    }

    Given("a fixed Dymit recruitment timestamp") {
        Then("persistence, domain, DTO, request, and response mappings preserve the exact instant") {
            val createdAt = Instant.parse("2026-08-01T14:59:59.123Z")
            val updatedAt = Instant.parse("2026-08-01T15:00:00.456Z")
            val recruitmentStart = Instant.parse("2026-08-02T00:00:00.789Z")
            val recruitmentEnd = Instant.parse("2026-08-09T00:00:00.987Z")
            val id = ObjectId("507f1f77bcf86cd799439011")
            val writerId = ObjectId("507f1f77bcf86cd799439012")
            val groupId = ObjectId("507f1f77bcf86cd799439013")
            val persistence = DymitStudyRecruitmentPersistenceDto(
                id = id,
                writerId = writerId,
                writerNickname = "writer",
                groupId = groupId,
                type = StudyRecruitmentType.DYMIT,
                title = "title",
                description = "description",
                purpose = "purpose",
                recruitmentStatus = DymitStudyRecruitmentStatus.RECRUITING,
                recruitmentStart = recruitmentStart,
                recruitmentEnd = recruitmentEnd,
                targetMember = "target",
                studyFormat = "online",
                contact = Contact("https://example.com", "contact"),
                tags = emptyList(),
                createdAt = createdAt,
                updatedAt = updatedAt,
                isDeleted = false
            )
            val writer = DymitStudyRecruitmentMemberDto(writerId, "https://example.com/profile.png")
            val domain = persistence.toDomain()
            val dto = DymitStudyRecruitmentDto.from(persistence, writer)
            val response = DymitStudyRecruitmentResponse.from(dto)
            val request = CreateStudyRecruitmentRequest(
                groupId = groupId.toHexString(),
                title = "title",
                description = "description",
                purpose = "purpose",
                targetMember = "target",
                studyFormat = "online",
                contact = Contact("https://example.com", "contact"),
                recruitmentStart = recruitmentStart,
                recruitmentEnd = recruitmentEnd
            )

            domain.createdAt shouldBe createdAt
            domain.updatedAt shouldBe updatedAt
            dto.createdAt shouldBe createdAt
            dto.updatedAt shouldBe updatedAt
            dto.recruitmentStart shouldBe recruitmentStart
            dto.recruitmentEnd shouldBe recruitmentEnd
            response.createdAt shouldBe createdAt
            response.updatedAt shouldBe updatedAt
            response.recruitmentStart shouldBe recruitmentStart
            response.recruitmentEnd shouldBe recruitmentEnd
            request.toCommand().recruitmentStart shouldBe recruitmentStart
            request.toCommand().recruitmentEnd shouldBe recruitmentEnd
        }
    }

    Given("fixed UTC instants around Korean midnight") {
        Then("the daily window changes dates at midnight without a nine-hour shift") {
            val beforeMidnight = DailyStatisticsWindowCalculator.calculate(
                Instant.parse("2026-08-01T14:59:59Z")
            )
            val atMidnight = DailyStatisticsWindowCalculator.calculate(
                Instant.parse("2026-08-01T15:00:00Z")
            )

            beforeMidnight.statisticDate.toString() shouldBe "2026-07-31"
            readInstant(beforeMidnight, "windowStart") shouldBe Instant.parse("2026-07-30T19:00:00Z")
            readInstant(beforeMidnight, "windowEnd") shouldBe Instant.parse("2026-07-31T19:00:00Z")
            atMidnight.statisticDate.toString() shouldBe "2026-08-01"
            readInstant(atMidnight, "windowStart") shouldBe Instant.parse("2026-07-31T19:00:00Z")
            readInstant(atMidnight, "windowEnd") shouldBe Instant.parse("2026-08-01T19:00:00Z")
        }
    }

    Given("the Quartz schedules") {
        Then("the KST 09:00, hourly, 04:00, and 05:00 jobs fire at the correct UTC instants") {
            val config = QuartzConfig()
            val after = Instant.parse("2026-08-01T14:59:59Z")

            val dailyReminder = config.triggerOn9AMUTC9(config.dailyScheduleReminderJobDetail()) as CronTrigger
            val hourlyReminder = config.triggerEveryHour(config.hourlyScheduleReminderJobDetail()) as CronTrigger
            val statistics = config.memberDailyStatisticsTrigger(config.memberDailyStatisticsJobDetail()) as CronTrigger
            val report = config.dailyStatisticsReportTrigger(config.dailyStatisticsReportJobDetail()) as CronTrigger

            listOf(dailyReminder, hourlyReminder, statistics, report)
                .map { it.timeZone.id }
                .shouldContainExactly("Asia/Seoul", "Asia/Seoul", "Asia/Seoul", "Asia/Seoul")
            nextFire(dailyReminder, after) shouldBe Instant.parse("2026-08-02T00:00:00Z")
            nextFire(hourlyReminder, after) shouldBe Instant.parse("2026-08-01T15:00:00Z")
            nextFire(statistics, Instant.parse("2026-08-01T18:59:59Z")) shouldBe
                Instant.parse("2026-08-01T19:00:00Z")
            nextFire(report, Instant.parse("2026-08-01T19:59:59Z")) shouldBe
                Instant.parse("2026-08-01T20:00:00Z")
        }
    }
})

private fun findField(type: Class<*>, name: String): Field {
    var current: Class<*>? = type
    while (current != null) {
        current.getDeclaredFieldOrNull(name)?.let { return it }
        current = current.superclass
    }
    error("Could not find field '$name' in ${type.name} or its superclasses")
}

private fun Class<*>.getDeclaredFieldOrNull(name: String): Field? = try {
    getDeclaredField(name)
} catch (_: NoSuchFieldException) {
    null
}

private fun readInstant(target: Any, name: String): Instant =
    findField(target.javaClass, name).apply { isAccessible = true }.get(target) as Instant

private fun nextFire(trigger: CronTrigger, after: Instant): Instant =
    CronExpression(trigger.cronExpression).apply {
        timeZone = trigger.timeZone
    }.getNextValidTimeAfter(Date.from(after)).toInstant()
