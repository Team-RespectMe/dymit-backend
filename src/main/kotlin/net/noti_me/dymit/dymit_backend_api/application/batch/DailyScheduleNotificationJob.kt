package net.noti_me.dymit.dymit_backend_api.application.batch

import net.noti_me.dymit.dymit_backend_api.application.batch.events.DailyScheduleNotificationEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import org.quartz.DisallowConcurrentExecution
import org.springframework.stereotype.Component
import org.quartz.JobExecutionContext
import org.quartz.Job
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

/**
 * 매일 오전 9시에 +15시간 이내에 예정된 스터디 일정에 대해 알림 및 피드 생성 이벤트를 
 * 발생시키는 배치 작업
 * 서버는 UTC+0 시간을 기준으로 동작하는 것을 상정하여 개발되었다.
 * 즉 한국 시간(UTC+9)으로 매일 오전 9시에 작업이 실행되도록 설정하려면
 * UTC+0 기준으로 매일 자정(00:00)에 작업이 실행되되도록 스케줄링 해야 한다.
 */
@Component
@DisallowConcurrentExecution
class DailyScheduleNotificationJob(
    private val loadGroupPort: LoadStudyGroupPort,
    private val studyScheduleRepository: StudyScheduleRepository,
    private val scheduleParticipantRepository: ScheduleParticipantRepository,
    private val eventPublisher: ApplicationEventPublisher
) : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val BATCH_SIZE = 1000

    override fun execute(context: JobExecutionContext) {
        val now = LocalDateTime.now()
        var cursor: ObjectId? = null

        do {
            val  schedules = pullStudySchedulesForToday(
                cursor = cursor,
                current = now
            )

            schedules.forEach { schedule ->
                processSchedule(schedule)
            }

            if ( schedules.size >= BATCH_SIZE ) {
                cursor = schedules.last().id
            }
        } while ( schedules.size >= BATCH_SIZE )
    }

    private fun pullStudySchedulesForToday(current: LocalDateTime, cursor: ObjectId?): List<StudySchedule> {
        val schedules = studyScheduleRepository.findByScheduleAtBetweenCursorPagination(
            start = current.withHour(0).withMinute(0).withSecond(0).withNano(0),
            end = current.withHour(15).withMinute(0).withSecond(0).withNano(0),
            cursor = cursor,
            limit = BATCH_SIZE
        )

        return schedules
    }

    private fun processSchedule(schedule: StudySchedule) {
        val participants = pullScheduleParticipants(schedule)
        val memberIds = participants.mapNotNull { it.memberId }.distinct()
        if ( memberIds.isEmpty() ) {
            logger.info("No participants found for schedule id: ${schedule.id}")
            return
        }

        val group = loadGroupPort.loadByGroupId(schedule.groupId.toString())

        if ( group == null ) {
            logger.warn("Study group not found for schedule id: ${schedule.id}, group id: ${schedule.groupId}")
            return
        }

        val event = DailyScheduleNotificationEvent(
            group = group,
            schedule = schedule,
            memberIds = memberIds
        )
        eventPublisher.publishEvent(event)
    }

    private fun pullScheduleParticipants(schedule: StudySchedule): List<ScheduleParticipant> {
        val scheduleId = schedule.id ?: return emptyList()
        return scheduleParticipantRepository.getByScheduleId(scheduleId)
    }
}

