package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetGroupTasksQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetGroupTasksUseCase
import org.springframework.stereotype.Service

/**
 * 그룹 과제 목록 조회 유즈케이스 구현체입니다.
 */
@Service
class GetGroupTasksUseCaseImpl(
    private val support: TaskServiceSupport
) : GetGroupTasksUseCase {

    override fun execute(query: GetGroupTasksQuery): List<TaskDto> {
        val (memberInfo, groupId) = query
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")

        support.requireGroupMember(groupIdObjectId, memberId)
        return support.loadTasksByGroup(groupIdObjectId)
            .sortedByDescending { it.expireAt }
            .map { support.toTaskDto(it, groupIdObjectId, allowMissingAssignee = true) }
    }
}
