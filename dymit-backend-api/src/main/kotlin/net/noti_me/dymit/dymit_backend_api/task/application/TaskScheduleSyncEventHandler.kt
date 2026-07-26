package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipatedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleParticipationCanceledEventDto
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskCreatedBroadcastEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 스케줄 참여 이벤트에 따른 과제 대상자 동기화 핸들러입니다.
 */
@Component
@Async
class TaskScheduleSyncEventHandler(
    private val taskService: TaskService,
    private val support: TaskServiceSupport,
    private val eventPublisher: ApplicationEventPublisher
) {

    /**
     * 일정 참여 시 기존 사전 과제 대상자를 동기화하고 신규 참여자에게 과제 알림을 발행합니다.
     *
     * @param event 일정 참여 이벤트
     */
    @EventListener
    fun onScheduleParticipated(event: StudyScheduleParticipatedEventDto) {
        taskService.syncParticipatedScheduleTasks(
            scheduleId = event.schedule.id,
            memberId = event.member.memberId
        )
            .forEach { task ->
                eventPublisher.publishEvent(
                    TaskCreatedBroadcastEvent(
                        group = support.loadGroup(event.group.id),
                        taskId = task.taskId,
                        memberIds = listOf(org.bson.types.ObjectId(event.member.memberId))
                    )
                )
            }
    }

    /**
     * 일정 참여 취소 시 기존 사전 과제 대상자 연결을 제거합니다.
     *
     * @param event 일정 참여 취소 이벤트
     */
    @EventListener
    fun onScheduleParticipateCanceled(event: StudyScheduleParticipationCanceledEventDto) {
        taskService.removeAssigneeFromPreTasks(
            scheduleId = event.schedule.id,
            memberId = event.member.memberId
        )
    }

    /**
     * 일정 취소 시 연결된 과제를 제거합니다.
     *
     * @param event 일정 취소 이벤트
     */
    @EventListener
    fun onScheduleCanceled(event: StudyScheduleCanceledEventDto) {
        taskService.removeTasksByCanceledSchedule(
            scheduleId = event.schedule.id,
            groupId = event.group.id
        )
    }
}
