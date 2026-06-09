package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 대상 목록 조회 유즈케이스입니다.
 */
interface GetTaskAssigneesUseCase {

    /**
     * 과제에 등록된 제출 대상 목록을 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param taskId 과제 ID
     * @return 과제 제출 대상 DTO 목록
     */
    fun getTaskAssignees(memberInfo: MemberInfo, taskId: String): List<TaskAssigneeDto>
}
