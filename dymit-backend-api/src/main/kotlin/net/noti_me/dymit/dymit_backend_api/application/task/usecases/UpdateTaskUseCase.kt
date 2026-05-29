package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 수정 유즈케이스입니다.
 */
interface UpdateTaskUseCase {

    /**
     * 과제를 수정합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param command 과제 수정 명령 DTO
     * @return 수정된 과제 DTO
     */
    fun updateTask(memberInfo: MemberInfo, groupId: String, taskId: String, command: UpdateTaskCommand): TaskDto
}
