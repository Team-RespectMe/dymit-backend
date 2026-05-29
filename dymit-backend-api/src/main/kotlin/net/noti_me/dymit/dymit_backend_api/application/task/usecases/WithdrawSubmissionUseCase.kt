package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 철회 유즈케이스입니다.
 */
interface WithdrawSubmissionUseCase {

    /**
     * 과제 제출을 철회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param submissionId 제출 ID
     */
    fun withdrawSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String)
}
