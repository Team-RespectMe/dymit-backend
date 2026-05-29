package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 제출 댓글 생성 유즈케이스입니다.
 */
interface CreateSubmissionCommentUseCase {

    /**
     * 과제 제출 댓글을 생성합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     * @param submissionId 제출 ID
     * @param command 댓글 생성 명령 DTO
     * @return 생성된 댓글 DTO
     */
    fun createSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: CreateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto
}
