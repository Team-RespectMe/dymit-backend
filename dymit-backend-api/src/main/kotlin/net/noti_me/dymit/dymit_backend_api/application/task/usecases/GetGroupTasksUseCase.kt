package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 그룹 과제 목록 조회 유즈케이스입니다.
 */
interface GetGroupTasksUseCase {

    /**
     * 스터디 그룹의 과제 목록을 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @return 과제 DTO 목록
     */
    fun getGroupTasks(memberInfo: MemberInfo, groupId: String): List<TaskDto>
}
