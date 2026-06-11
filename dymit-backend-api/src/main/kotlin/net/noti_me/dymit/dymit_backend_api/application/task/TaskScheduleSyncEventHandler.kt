package net.noti_me.dymit.dymit_backend_api.application.task

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event.ScheduleCancelParticipateEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event.ScheduleParticipateEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event.StudyScheduleCanceledEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedBroadcastEvent
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
    fun onScheduleParticipated(event: ScheduleParticipateEvent) {
        taskService.syncParticipatedScheduleTasks(
            scheduleId = event.schedule.identifier,
            memberId = event.member.memberId.toHexString()
        )
            .forEach { task ->
                eventPublisher.publishEvent(
                    TaskCreatedBroadcastEvent(
                        group = event.group,
                        task = task,
                        memberIds = listOf(event.member.memberId)
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
    fun onScheduleParticipateCanceled(event: ScheduleCancelParticipateEvent) {
        taskService.removeAssigneeFromPreTasks(
            scheduleId = event.schedule.identifier,
            memberId = event.member.memberId.toHexString()
        )
    }

    /**
     * 일정 취소 시 연결된 과제를 제거합니다.
     *
     * @param event 일정 취소 이벤트
     */
    @EventListener
    fun onScheduleCanceled(event: StudyScheduleCanceledEvent) {
        taskService.removeTasksByCanceledSchedule(
            scheduleId = event.schedule.identifier,
            groupId = event.group.identifier
        )
    }
}
