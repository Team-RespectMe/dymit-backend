package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 댓글 목록 조회 유즈케이스입니다.
 */
interface GetSubmissionCommentsUseCase {

    /**
     * 과제 제출 댓글 목록을 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param submissionId 제출 ID
     * @return 댓글 DTO 목록
     */
    fun getSubmissionComments(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    ): List<TaskSubmissionCommentDto>
}
