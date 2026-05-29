package net.noti_me.dymit.dymit_backend_api.application.task

import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event.ScheduleCancelParticipateEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event.ScheduleParticipateEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.event.StudyScheduleCanceledEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 스케줄 참여 이벤트에 따른 과제 대상자 동기화 핸들러입니다.
 */
@Component
@Async
class TaskScheduleSyncEventHandler(
    private val taskService: TaskService
) {

    @EventListener
    fun onScheduleParticipated(event: ScheduleParticipateEvent) {
        taskService.addAssigneeToPreTasks(
            scheduleId = event.schedule.identifier,
            memberId = event.member.memberId.toHexString()
        )
    }

    @EventListener
    fun onScheduleParticipateCanceled(event: ScheduleCancelParticipateEvent) {
        taskService.removeAssigneeFromPreTasks(
            scheduleId = event.schedule.identifier,
            memberId = event.member.memberId.toHexString()
        )
    }

    @EventListener
    fun onScheduleCanceled(event: StudyScheduleCanceledEvent) {
        taskService.removeTasksByCanceledSchedule(
            scheduleId = event.schedule.identifier,
            groupId = event.group.identifier
        )
    }
}
