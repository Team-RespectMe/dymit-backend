package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 단건 조회 유즈케이스입니다.
 */
interface GetTaskSubmissionUseCase {

    /**
     * 과제 제출 단건을 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param memberId 제출 대상 멤버 ID
     * @return 제출 DTO
     */
    fun getTaskSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        memberId: String
    ): TaskSubmissionDto
}
