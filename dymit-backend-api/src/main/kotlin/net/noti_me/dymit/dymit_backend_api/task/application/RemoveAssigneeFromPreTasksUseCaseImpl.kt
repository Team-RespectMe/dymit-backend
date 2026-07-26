package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveAssigneeFromPreTasksCommand
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.RemoveAssigneeFromPreTasksUseCase
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import org.springframework.stereotype.Service

/**
 * 사전 과제 대상자 제거 유즈케이스 구현체입니다.
 */
@Service
class RemoveAssigneeFromPreTasksUseCaseImpl(
    private val support: TaskServiceSupport
) : RemoveAssigneeFromPreTasksUseCase {

    override fun execute(command: RemoveAssigneeFromPreTasksCommand) {
        val (scheduleId, memberId) = command
        val scheduleObjectId = TaskUseCaseObjectIdParser.parse(scheduleId, "scheduleId")
        val memberObjectId = TaskUseCaseObjectIdParser.parse(memberId, "memberId")

        support.loadTasksBySchedule(scheduleObjectId, TaskType.PRE)
            .forEach { task ->
                support.removeAssigneeWithSubmissionCleanup(task.id!!, memberObjectId)
            }
    }
}
