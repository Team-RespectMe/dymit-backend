package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskDeletionSupport
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.RemoveTaskUseCase
import org.springframework.stereotype.Service

/**
 * 과제 삭제 유즈케이스 구현체입니다.
 */
@Service
class RemoveTaskUseCaseImpl(
    private val support: TaskServiceSupport,
    private val taskDeletionSupport: TaskDeletionSupport
) : RemoveTaskUseCase {

    override fun execute(command: RemoveTaskCommand) {
        val (memberInfo, groupId, taskId) = command
        val group = support.loadGroup(groupId)
        support.checkOwner(memberInfo, group)

        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkTaskActionAllowedBySchedule(task)
        taskDeletionSupport.cascadeDeleteTask(
            task = task,
            groupId = groupIdObjectId,
            group = group,
            deletedByScheduleEvent = false
        )
    }
}
