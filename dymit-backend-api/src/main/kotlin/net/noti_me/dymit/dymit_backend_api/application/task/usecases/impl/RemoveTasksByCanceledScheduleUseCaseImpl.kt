package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskDeletionSupport
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.RemoveTasksByCanceledScheduleUseCase
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import org.springframework.stereotype.Service

/**
 * 취소된 일정 연관 과제 삭제 유즈케이스 구현체입니다.
 */
@Service
class RemoveTasksByCanceledScheduleUseCaseImpl(
    private val support: TaskServiceSupport,
    private val taskDeletionSupport: TaskDeletionSupport
) : RemoveTasksByCanceledScheduleUseCase {

    override fun removeTasksByCanceledSchedule(scheduleId: String, groupId: String) {
        val scheduleObjectId = TaskUseCaseObjectIdParser.parse(scheduleId, "scheduleId")
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")

        support.loadTasksBySchedule(scheduleObjectId, TaskType.PRE)
            .forEach { task ->
                taskDeletionSupport.cascadeDeleteTask(
                    task = task,
                    groupId = groupIdObjectId,
                    deletedByScheduleEvent = true
                )
            }
    }
}
