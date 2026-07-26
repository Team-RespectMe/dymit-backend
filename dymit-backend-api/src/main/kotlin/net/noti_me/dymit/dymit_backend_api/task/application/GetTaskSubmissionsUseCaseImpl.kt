package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskSubmissionsQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetTaskSubmissionsUseCase
import org.springframework.stereotype.Service

/**
 * 과제 제출 목록 조회 유즈케이스 구현체입니다.
 */
@Service
class GetTaskSubmissionsUseCaseImpl(
    private val support: TaskServiceSupport
) : GetTaskSubmissionsUseCase {

    override fun execute(query: GetTaskSubmissionsQuery): List<TaskSubmissionDto> {
        val (memberInfo, groupId, taskId) = query
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")

        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)

        return support.loadSubmissionsByTask(task.id!!)
            .map { support.toSubmissionDto(it, groupIdObjectId) }
    }
}
