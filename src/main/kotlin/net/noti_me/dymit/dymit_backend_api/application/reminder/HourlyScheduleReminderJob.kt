package net.noti_me.dymit.dymit_backend_api.application.reminder

import org.quartz.DisallowConcurrentExecution
import org.springframework.stereotype.Component
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.application.reminder.events.HourlyScheduleReminderEvent
import org.bson.types.ObjectId
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

/**
 * 매 시간 정각에 +1시간 이내에 예정된 스터디 일정에 대해 알림 및 피드 생성 이벤트를
 * 발생시키는 배치 작업
 * 서버는 UTC+0 시간을 기준으로 동작하는 것을 상정하여 개발되었다.
 * 즉 한국 시간(UTC+9)으로 매 시간 정각에 작업이 실행되도록 설정하려면
 * UTC+0 기준으로 매 시간 51분에 작업이 실행되도록 스케줄링 해야 한다.
 */
@Component
@DisallowConcurrentExecution
class HourlyScheduleReminderJob(
    private val loadGroupPort: LoadStudyGroupPort,
    private val studyScheduleRepository: StudyScheduleRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val scheduleParticipantRepository: ScheduleParticipantRepository
): Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val BATCH_SIZE = 1000

    override fun execute(context: JobExecutionContext) {
        val now = LocalDateTime.now()
        val start = now.withMinute(0)
            .withSecond(0)
            .withNano(0)
        var cursor: ObjectId? = null

        do {
            val schedules = studyScheduleRepository.findByScheduleAtBetweenCursorPagination(
                start = start,
                end = start.plusHours(1).withMinute(0).withSecond(0).withNano(0),
                cursor = cursor,
                limit = BATCH_SIZE
            )

            schedules.forEach { schedule ->
                processSchedule(schedule)
            }

            if ( schedules.size >= BATCH_SIZE ) {
                cursor = schedules.last().id
            }
        } while ( schedules.size >= BATCH_SIZE ) 
    }

    private fun processSchedule(schedule: StudySchedule) {
        val participants = scheduleParticipantRepository.getByScheduleId(schedule.id!!)
        if ( participants.isEmpty() ) {
            logger.info("No participants for schedule ${schedule.id}, skipping...")
            return
        }

        val group = loadGroupPort.loadByGroupId(schedule.groupId.toHexString())

        if (group == null) {
            logger.warn("Study group ${schedule.groupId} not found for schedule ${schedule.id}, skipping...")
            return
        }  

        val event = HourlyScheduleReminderEvent(
            group = group,
            schedule = schedule,
            memberIds = participants.mapNotNull { it.memberId }.distinct()
        )
        eventPublisher.publishEvent(event)
    }
}

