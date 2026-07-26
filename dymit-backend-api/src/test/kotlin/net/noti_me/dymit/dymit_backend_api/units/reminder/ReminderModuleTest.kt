package net.noti_me.dymit.dymit_backend_api.units.reminder

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordQuartzLogger
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.ReminderStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.dto.ReminderStudyGroupDto
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.ReminderStudySchedulePort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto.ReminderStudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.reminder.application.usecase.DailyScheduleReminderJob
import net.noti_me.dymit.dymit_backend_api.reminder.application.usecase.HourlyScheduleReminderJob
import net.noti_me.dymit.dymit_backend_api.reminder.domain.DailyScheduleReminderEvent
import net.noti_me.dymit.dymit_backend_api.reminder.domain.HourlyScheduleReminderEvent
import org.bson.types.ObjectId
import org.quartz.JobExecutionContext
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

internal class ReminderModuleTest : BehaviorSpec() {

    private val groupPort = mockk<ReminderStudyGroupPort>()
    private val schedulePort = mockk<ReminderStudySchedulePort>()
    private val publisher = mockk<ApplicationEventPublisher>()
    private val quartzLogger = mockk<DiscordQuartzLogger>()
    private val context = mockk<JobExecutionContext>()

    init {
        afterEach { clearAllMocks() }

        given("the daily reminder job") {
            `when`("a full batch is followed by an empty batch") {
                then("it passes the last schedule id as the next query cursor") {
                    val firstBatch = List(1000) { schedule() }
                    every { quartzLogger.log(any(), any()) } just runs
                    every { schedulePort.findByScheduleAtBetween(any(), any(), isNull(), 1000) } returns firstBatch
                    every { schedulePort.findByScheduleAtBetween(any(), any(), firstBatch.last().id, 1000) } returns emptyList()
                    every { schedulePort.getParticipantMemberIds(any()) } returns emptyList()

                    DailyScheduleReminderJob(groupPort, schedulePort, publisher, quartzLogger).execute(context)

                    verify(exactly = 1) { schedulePort.findByScheduleAtBetween(any(), any(), firstBatch.last().id, 1000) }
                }
            }

            `when`("a schedule has duplicate participants and a group") {
                then("it uses the daily window and publishes one deduplicated event") {
                    val schedule = schedule()
                    val starts = slot<LocalDateTime>()
                    val ends = slot<LocalDateTime>()
                    val event = slot<DailyScheduleReminderEvent>()
                    every { quartzLogger.log(any(), any()) } just runs
                    every { schedulePort.findByScheduleAtBetween(capture(starts), capture(ends), null, 1000) } returns listOf(schedule)
                    every { schedulePort.getParticipantMemberIds(schedule.id) } returns listOf(ObjectId.get(), ObjectId.get(), ObjectId.get()).let { listOf(it[0], it[0], it[1]) }
                    every { groupPort.loadByGroupId(schedule.groupId) } returns group(schedule.groupId)
                    every { publisher.publishEvent(capture(event)) } just runs

                    DailyScheduleReminderJob(groupPort, schedulePort, publisher, quartzLogger).execute(context)

                    starts.captured.hour shouldBe 0
                    starts.captured.minute shouldBe 0
                    ends.captured.hour shouldBe 15
                    event.captured.toPersonalPushMessages().size shouldBe 2
                    event.captured.toPersonalFeedData().first().eventName shouldBe "DAILY_SCHEDULE_REMINDER"
                }
            }

            `when`("there are no participants or the group is missing") {
                then("it skips event publication") {
                    val emptyParticipants = schedule()
                    val missingGroup = schedule()
                    every { quartzLogger.log(any(), any()) } just runs
                    every { schedulePort.findByScheduleAtBetween(any(), any(), null, 1000) } returns listOf(emptyParticipants, missingGroup)
                    every { schedulePort.getParticipantMemberIds(emptyParticipants.id) } returns emptyList()
                    every { schedulePort.getParticipantMemberIds(missingGroup.id) } returns listOf(ObjectId.get())
                    every { groupPort.loadByGroupId(missingGroup.groupId) } returns null

                    DailyScheduleReminderJob(groupPort, schedulePort, publisher, quartzLogger).execute(context)

                    verify(exactly = 0) { publisher.publishEvent(any()) }
                }
            }
        }

        given("the hourly reminder job") {
            `when`("a full batch is followed by an empty batch") {
                then("it passes the last schedule id as the next query cursor") {
                    val firstBatch = List(1000) { schedule() }
                    every { quartzLogger.log(any(), any()) } just runs
                    every { schedulePort.findByScheduleAtBetween(any(), any(), isNull(), 1000) } returns firstBatch
                    every { schedulePort.findByScheduleAtBetween(any(), any(), firstBatch.last().id, 1000) } returns emptyList()
                    every { schedulePort.getParticipantMemberIds(any()) } returns emptyList()

                    HourlyScheduleReminderJob(groupPort, schedulePort, publisher, quartzLogger).execute(context)

                    verify(exactly = 1) { schedulePort.findByScheduleAtBetween(any(), any(), firstBatch.last().id, 1000) }
                }
            }

            `when`("a schedule is within the current-hour batch") {
                then("it uses a one-hour window and publishes a deduplicated hourly event") {
                    val schedule = schedule()
                    val starts = slot<LocalDateTime>()
                    val ends = slot<LocalDateTime>()
                    val event = slot<HourlyScheduleReminderEvent>()
                    val memberId = ObjectId.get()
                    every { quartzLogger.log(any(), any()) } just runs
                    every { schedulePort.findByScheduleAtBetween(capture(starts), capture(ends), null, 1000) } returns listOf(schedule)
                    every { schedulePort.getParticipantMemberIds(schedule.id) } returns listOf(memberId, memberId)
                    every { groupPort.loadByGroupId(schedule.groupId) } returns group(schedule.groupId)
                    every { publisher.publishEvent(capture(event)) } just runs

                    HourlyScheduleReminderJob(groupPort, schedulePort, publisher, quartzLogger).execute(context)

                    starts.captured.minute shouldBe 0
                    ends.captured shouldBe starts.captured.plusHours(1)
                    event.captured.memberIds shouldBe listOf(memberId)
                    event.captured.toPersonalPushMessages().single().eventName shouldBe "HOURLY_SCHEDULE_REMINDER"
                }
            }
        }

        given("reminder events") {
            `when`("their push and feed data is created") {
                then("they preserve the existing text, data, and resource values") {
                    val group = group()
                    val schedule = schedule(group.id)
                    val memberId = ObjectId.get()
                    val daily = DailyScheduleReminderEvent(group, schedule, listOf(memberId))
                    val hourly = HourlyScheduleReminderEvent(group, schedule, listOf(memberId))

                    daily.toPersonalPushMessages().single().body shouldBe "${group.name} 의 ${schedule.session} 회차 스터디가 오늘 예정되어 있어요!"
                    daily.toPersonalFeedData().single().resources.map { it.resourceId } shouldBe listOf(group.id.toString(), schedule.id.toString(), memberId.toString())
                    hourly.toPersonalPushMessages().single().data shouldBe mapOf(
                        "groupId" to group.id.toHexString(),
                        "scheduleId" to schedule.id.toHexString(),
                        "ownerId" to group.ownerId.toString()
                    )
                }
            }
        }
    }

    private fun group(id: ObjectId = ObjectId.get()): ReminderStudyGroupDto {
        return ReminderStudyGroupDto(id, ObjectId.get(), "Dymit", "thumbnail")
    }

    private fun schedule(
        groupId: ObjectId = ObjectId.get(),
        id: ObjectId = ObjectId.get()
    ): ReminderStudyScheduleDto {
        return ReminderStudyScheduleDto(id, groupId, "Schedule", 3, LocalDateTime.of(2026, 7, 26, 12, 0))
    }
}
