package net.noti_me.dymit.dymit_backend_api.reminder.application.usecase

import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordQuartzLogger
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.ReminderStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.ReminderStudySchedulePort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_schedule.dto.ReminderStudyScheduleDto
import net.noti_me.dymit.dymit_backend_api.reminder.domain.HourlyScheduleReminderEvent
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
 * 매 시간 정각에 한 시간 이내 예정 일정의 알림 이벤트를 발생시킵니다.
 */
@Component
@DisallowConcurrentExecution
class HourlyScheduleReminderJob(
    private val loadGroupPort: ReminderStudyGroupPort,
    private val studySchedulePort: ReminderStudySchedulePort,
    private val eventPublisher: ApplicationEventPublisher,
    private val quartzLogger: DiscordQuartzLogger
) : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 기존 시간별 일정 알림 배치를 실행합니다.
     */
    override fun execute(context: JobExecutionContext) {
        val now = Instant.now()
        val start = now.atZone(KOREA_ZONE)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .toInstant()
        var cursor: ObjectId? = null

        quartzLogger.log(
            title = "Hourly Schedule Reminder Job Started",
            message = "Starting to process study schedules from $start to ${start.plusSeconds(3600)}"
        )

        do {
            val schedules = studySchedulePort.findByScheduleAtBetween(
                start = start,
                end = start.plusSeconds(3600),
                cursor = cursor,
                limit = BATCH_SIZE
            )

            schedules.forEach(::processSchedule)

            if (schedules.size >= BATCH_SIZE) {
                cursor = schedules.last().id
            }
        } while (schedules.size >= BATCH_SIZE)

        val end = Instant.now()
        quartzLogger.log(
            title = "Hourly Schedule Reminder Job Completed",
            message = "Completed processing study schedules for hourly reminders. Started at: $now, ended at: $end"
        )
    }

    private fun processSchedule(schedule: ReminderStudyScheduleDto) {
        val participantIds = studySchedulePort.getParticipantMemberIds(schedule.id)
        if (participantIds.isEmpty()) {
            logger.info("No participants for schedule ${schedule.id}, skipping...")
            return
        }

        val group = loadGroupPort.loadByGroupId(schedule.groupId)
        if (group == null) {
            logger.warn("Study group ${schedule.groupId} not found for schedule ${schedule.id}, skipping...")
            return
        }

        eventPublisher.publishEvent(
            HourlyScheduleReminderEvent(
                group = group,
                schedule = schedule,
                memberIds = participantIds.distinct()
            )
        )
    }

    private companion object {
        const val BATCH_SIZE = 1000
        val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
