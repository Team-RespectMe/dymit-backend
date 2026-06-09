package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskDeletionSupport
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.RemoveTaskUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 과제 삭제 유즈케이스 구현체입니다.
 */
@Service
class RemoveTaskUseCaseImpl(
    private val support: TaskServiceSupport,
    private val taskDeletionSupport: TaskDeletionSupport
) : RemoveTaskUseCase {

    override fun removeTask(memberInfo: MemberInfo, groupId: String, taskId: String) {
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
