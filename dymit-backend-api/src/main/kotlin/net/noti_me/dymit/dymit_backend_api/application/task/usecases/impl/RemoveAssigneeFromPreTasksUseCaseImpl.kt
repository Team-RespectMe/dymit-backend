package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.RemoveAssigneeFromPreTasksUseCase
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import org.springframework.stereotype.Service

/**
 * 사전 과제 대상자 제거 유즈케이스 구현체입니다.
 */
@Service
class RemoveAssigneeFromPreTasksUseCaseImpl(
    private val support: TaskServiceSupport
) : RemoveAssigneeFromPreTasksUseCase {

    override fun removeAssigneeFromPreTasks(scheduleId: String, memberId: String) {
        val scheduleObjectId = TaskUseCaseObjectIdParser.parse(scheduleId, "scheduleId")
        val memberObjectId = TaskUseCaseObjectIdParser.parse(memberId, "memberId")

        support.loadTasksBySchedule(scheduleObjectId, TaskType.PRE)
            .forEach { task ->
                support.removeAssigneeWithSubmissionCleanup(task.id!!, memberObjectId)
            }
    }
}
