package net.noti_me.dymit.dymit_backend_api.application.task

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskDeletedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskDeletedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskModifiedBroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskModifiedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 과제 도메인 이벤트를 브로드캐스트 이벤트로 변환하는 핸들러입니다.
 */
@Component
@Async
class TaskNotificationPreparationEventHandler(
    private val support: TaskServiceSupport,
    private val eventPublisher: ApplicationEventPublisher
) {

    /**
     * 과제 생성 이벤트를 수신해 브로드캐스트 이벤트를 발행합니다.
     *
     * @param event 과제 생성 이벤트
     */
    @EventListener
    fun onTaskCreated(event: TaskCreatedEvent) {
        eventPublisher.publishEvent(
            TaskCreatedBroadcastEvent(
                group = event.group,
                task = event.task,
                memberIds = support.loadAssigneeMemberIdsByTask(event.taskId)
            )
        )
    }

    /**
     * 과제 수정 이벤트를 수신해 브로드캐스트 이벤트를 발행합니다.
     *
     * @param event 과제 수정 이벤트
     */
    @EventListener
    fun onTaskModified(event: TaskModifiedEvent) {
        eventPublisher.publishEvent(
            TaskModifiedBroadcastEvent(
                group = event.group,
                task = event.task,
                memberIds = support.loadAssigneeMemberIdsByTask(event.taskId)
            )
        )
    }

    /**
     * 과제 삭제 이벤트를 수신해 브로드캐스트 이벤트를 발행합니다.
     *
     * @param event 과제 삭제 이벤트
     */
    @EventListener
    fun onTaskDeleted(event: TaskDeletedEvent) {
        eventPublisher.publishEvent(
            TaskDeletedBroadcastEvent(
                group = event.group ?: support.loadGroup(event.groupId.toHexString()),
                task = event.task,
                memberIds = event.assigneeMemberIds
            )
        )
    }
}
