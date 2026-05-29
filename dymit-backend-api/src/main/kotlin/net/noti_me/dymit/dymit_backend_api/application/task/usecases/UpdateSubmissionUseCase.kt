package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 수정 유즈케이스입니다.
 */
interface UpdateSubmissionUseCase {

    /**
     * 과제 제출을 수정합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param submissionId 제출 ID
     * @param command 제출 수정 명령 DTO
     * @return 수정된 제출 DTO
     */
    fun updateSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: UpdateTaskSubmissionCommand
    ): TaskSubmissionDto
}
