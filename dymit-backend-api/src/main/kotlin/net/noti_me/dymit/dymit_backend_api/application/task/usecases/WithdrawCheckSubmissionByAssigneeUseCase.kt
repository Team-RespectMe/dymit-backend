package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 체크형 과제 제출 철회 유즈케이스입니다.
 */
interface WithdrawCheckSubmissionByAssigneeUseCase {

    /**
     * 체크형 과제 제출을 대상자 ID 기준으로 철회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param assigneeId 제출 대상자 ID
     */
    fun withdrawCheckSubmissionByAssignee(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        assigneeId: String
    )
}
