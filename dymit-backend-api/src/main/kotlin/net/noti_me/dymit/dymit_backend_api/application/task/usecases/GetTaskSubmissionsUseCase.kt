package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 목록 조회 유즈케이스입니다.
 */
interface GetTaskSubmissionsUseCase {

    /**
     * 과제 제출 목록을 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @return 제출 DTO 목록
     */
    fun getTaskSubmissions(memberInfo: MemberInfo, groupId: String, taskId: String): List<TaskSubmissionDto>
}
