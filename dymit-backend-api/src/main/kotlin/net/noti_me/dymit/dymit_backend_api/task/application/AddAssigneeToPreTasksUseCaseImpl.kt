package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.AddAssigneeToPreTasksCommand
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.AddAssigneeToPreTasksUseCase
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import org.springframework.stereotype.Service

/**
 * 사전 과제 대상자 추가 유즈케이스 구현체입니다.
 */
@Service
class AddAssigneeToPreTasksUseCaseImpl(
    private val support: TaskServiceSupport
) : AddAssigneeToPreTasksUseCase {

    override fun execute(command: AddAssigneeToPreTasksCommand) {
        val (scheduleId, memberId) = command
        val scheduleObjectId = TaskUseCaseObjectIdParser.parse(scheduleId, "scheduleId")
        val memberObjectId = TaskUseCaseObjectIdParser.parse(memberId, "memberId")

        support.loadTasksBySchedule(scheduleObjectId, TaskType.PRE)
            .forEach { task -> support.addAssigneeIfAbsent(task.id!!, memberObjectId) }
    }
}
