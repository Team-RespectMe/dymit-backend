package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 댓글 삭제 유즈케이스입니다.
 */
interface DeleteSubmissionCommentUseCase {

    /**
     * 과제 제출 댓글을 삭제합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param submissionId 제출 ID
     * @param commentId 댓글 ID
     */
    fun deleteSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String
    )
}
