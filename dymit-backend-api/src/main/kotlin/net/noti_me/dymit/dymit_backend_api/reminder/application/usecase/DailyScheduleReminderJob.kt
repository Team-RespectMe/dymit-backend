package net.noti_me.dymit.dymit_backend_api.reminder.application.usecase

import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordQuartzLogger
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.ReminderStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.ReminderStudySchedulePort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto.ReminderStudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.reminder.domain.DailyScheduleReminderEvent
import org.bson.types.ObjectId
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId

/**
 * 매일 오전 9시에 당일 15시 이전 예정 일정의 알림과 피드 이벤트를 발생시킵니다.
 */
@Component
@DisallowConcurrentExecution
class DailyScheduleReminderJob(
    private val loadGroupPort: ReminderStudyGroupPort,
    private val studySchedulePort: ReminderStudySchedulePort,
    private val eventPublisher: ApplicationEventPublisher,
    private val quartzLogger: DiscordQuartzLogger
) : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 기존 일일 일정 알림 배치를 실행합니다.
     */
    override fun execute(context: JobExecutionContext) {
        quartzLogger.log(
            title = "Daily Schedule Reminder Job Started",
            message = "Starting to process study schedules for daily reminders."
        )
        val now = Instant.now()
        var cursor: ObjectId? = null

        do {
            val schedules = pullStudySchedulesForToday(
                cursor = cursor,
                current = now
            )

            schedules.forEach(::processSchedule)

            if (schedules.size >= BATCH_SIZE) {
                cursor = schedules.last().id
            }
        } while (schedules.size >= BATCH_SIZE)

        val end = Instant.now()
        quartzLogger.log(
            title = "Daily Schedule Reminder Job Completed",
            message = "Completed processing study schedules for daily reminders. Started at: $now, ended at: $end"
        )
    }

    private fun pullStudySchedulesForToday(
        current: Instant,
        cursor: ObjectId?
    ): List<ReminderStudyScheduleDto> {
        return studySchedulePort.findByScheduleAtBetween(
            start = current.atZone(KOREA_ZONE)
                .toLocalDate()
                .atStartOfDay(KOREA_ZONE)
                .toInstant(),
            end = current.atZone(KOREA_ZONE)
                .toLocalDate()
                .atTime(15, 0)
                .atZone(KOREA_ZONE)
                .toInstant(),
            cursor = cursor,
            limit = BATCH_SIZE
        )
    }

    private fun processSchedule(schedule: ReminderStudyScheduleDto) {
        val memberIds = studySchedulePort
            .getParticipantMemberIds(schedule.id)
            .distinct()
        if (memberIds.isEmpty()) {
            logger.info("No participants found for schedule id: ${schedule.id}")
            return
        }

        val group = loadGroupPort.loadByGroupId(schedule.groupId)
        if (group == null) {
            logger.warn("Study group not found for schedule id: ${schedule.id}, group id: ${schedule.groupId}")
            return
        }

        eventPublisher.publishEvent(
            DailyScheduleReminderEvent(
                group = group,
                schedule = schedule,
                memberIds = memberIds
            )
        )
    }

    private companion object {
        const val BATCH_SIZE = 1000
        val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
