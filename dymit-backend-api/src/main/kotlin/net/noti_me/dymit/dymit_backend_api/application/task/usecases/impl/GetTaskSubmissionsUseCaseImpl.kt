package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetTaskSubmissionsUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 과제 제출 목록 조회 유즈케이스 구현체입니다.
 */
@Service
class GetTaskSubmissionsUseCaseImpl(
    private val support: TaskServiceSupport
) : GetTaskSubmissionsUseCase {

    override fun getTaskSubmissions(memberInfo: MemberInfo, groupId: String, taskId: String): List<TaskSubmissionDto> {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")

        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)

        return support.loadSubmissionsByTask(task.id!!)
            .map { support.toSubmissionDto(it, groupIdObjectId) }
    }
}
