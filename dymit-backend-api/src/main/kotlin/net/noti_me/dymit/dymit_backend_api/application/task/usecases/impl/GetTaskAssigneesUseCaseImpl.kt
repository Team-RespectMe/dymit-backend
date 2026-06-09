package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetTaskAssigneesUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 과제 제출 대상 목록 조회 유즈케이스 구현체입니다.
 */
@Service
class GetTaskAssigneesUseCaseImpl(
    private val support: TaskServiceSupport
) : GetTaskAssigneesUseCase {

    override fun getTaskAssignees(memberInfo: MemberInfo, taskId: String): List<TaskAssigneeDto> {
        val taskObjectId = TaskUseCaseObjectIdParser.parse(taskId, "taskId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")

        val task = support.loadTask(taskId)
        val schedule = support.loadSchedule(task.relatedScheduleId.toHexString())
        support.requireGroupMember(schedule.groupId, memberId)

        return support.toTaskAssigneeDtos(taskObjectId, schedule.groupId)
    }
}
