package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 상세 조회 유즈케이스입니다.
 */
interface GetTaskDetailUseCase {

    /**
     * 과제 상세를 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @return 과제 DTO
     */
    fun getTaskDetail(memberInfo: MemberInfo, groupId: String, taskId: String): TaskDto
}
