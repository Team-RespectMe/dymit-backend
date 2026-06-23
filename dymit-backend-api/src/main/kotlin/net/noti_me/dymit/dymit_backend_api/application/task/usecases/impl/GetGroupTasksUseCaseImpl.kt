package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetGroupTasksUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 그룹 과제 목록 조회 유즈케이스 구현체입니다.
 */
@Service
class GetGroupTasksUseCaseImpl(
    private val support: TaskServiceSupport
) : GetGroupTasksUseCase {

    override fun getGroupTasks(memberInfo: MemberInfo, groupId: String): List<TaskDto> {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")

        support.requireGroupMember(groupIdObjectId, memberId)
        return support.loadTasksByGroup(groupIdObjectId)
            .sortedByDescending { it.expireAt }
            .map { support.toTaskDto(it, groupIdObjectId) }
    }
}
