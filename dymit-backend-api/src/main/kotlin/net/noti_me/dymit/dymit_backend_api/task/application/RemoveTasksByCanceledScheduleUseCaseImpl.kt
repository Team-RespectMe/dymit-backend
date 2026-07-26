package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskDeletionSupport
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveTasksByCanceledScheduleCommand
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.RemoveTasksByCanceledScheduleUseCase
import org.springframework.stereotype.Service

/**
 * 취소된 일정 연관 과제 삭제 유즈케이스 구현체입니다.
 */
@Service
class RemoveTasksByCanceledScheduleUseCaseImpl(
    private val support: TaskServiceSupport,
    private val taskDeletionSupport: TaskDeletionSupport
) : RemoveTasksByCanceledScheduleUseCase {

    override fun execute(command: RemoveTasksByCanceledScheduleCommand) {
        val (scheduleId, groupId) = command
        val scheduleObjectId = TaskUseCaseObjectIdParser.parse(scheduleId, "scheduleId")
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")

        support.loadTasksBySchedule(scheduleObjectId)
            .forEach { task ->
                taskDeletionSupport.cascadeDeleteTask(
                    task = task,
                    groupId = groupIdObjectId,
                    deletedByScheduleEvent = true
                )
            }
    }
}
