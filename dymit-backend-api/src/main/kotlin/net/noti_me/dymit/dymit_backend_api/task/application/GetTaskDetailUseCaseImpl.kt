package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskDetailQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetTaskDetailUseCase
import org.springframework.stereotype.Service

/**
 * 과제 상세 조회 유즈케이스 구현체입니다.
 */
@Service
class GetTaskDetailUseCaseImpl(
    private val support: TaskServiceSupport
) : GetTaskDetailUseCase {

    override fun execute(query: GetTaskDetailQuery): TaskDto {
        val (memberInfo, groupId, taskId) = query
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")

        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        return support.toTaskDto(task, groupIdObjectId)
    }
}
