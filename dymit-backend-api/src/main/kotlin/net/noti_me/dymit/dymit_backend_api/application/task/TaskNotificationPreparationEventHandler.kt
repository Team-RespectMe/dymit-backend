package net.noti_me.dymit.dymit_backend_api.application.task

import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskDeletedEvent
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskModifiedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 과제 이벤트 기반 푸시/피드 준비 더미 핸들러입니다.
 */
@Component
@Async
class TaskNotificationPreparationEventHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onTaskCreated(event: TaskCreatedEvent) {
        logger.info(
            "[TASK_DUMMY_LISTENER] created taskId={}, groupId={}, scheduleId={}, taskAggregateId={}, groupAggregateId={}",
            event.taskId,
            event.groupId,
            event.scheduleId,
            event.task.id,
            event.group.id
        )
    }

    @EventListener
    fun onTaskModified(event: TaskModifiedEvent) {
        logger.info(
            "[TASK_DUMMY_LISTENER] modified taskId={}, groupId={}, scheduleId={}, taskAggregateId={}, groupAggregateId={}",
            event.taskId,
            event.groupId,
            event.scheduleId,
            event.task.id,
            event.group.id
        )
    }

    @EventListener
    fun onTaskDeleted(event: TaskDeletedEvent) {
        logger.info(
            "[TASK_DUMMY_LISTENER] deleted taskId={}, groupId={}, scheduleId={}, taskAggregateId={}, groupAggregateId={}, assigneeMemberIds={}, byScheduleEvent={}",
            event.taskId,
            event.groupId,
            event.scheduleId,
            event.task.id,
            event.group?.id,
            event.assigneeMemberIds,
            event.deletedByScheduleEvent
        )
    }
}
