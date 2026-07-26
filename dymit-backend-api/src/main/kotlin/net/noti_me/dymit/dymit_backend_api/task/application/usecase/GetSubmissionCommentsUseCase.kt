package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetSubmissionCommentsQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionCommentDto

/**
 * 과제 제출 댓글 목록 조회 유즈케이스입니다.
 */
interface GetSubmissionCommentsUseCase {

    /**
     * 과제 제출 댓글 목록을 조회합니다.
     *
     * @param query 댓글 목록 조회 입력
     * @return 댓글 DTO 목록
     */
    fun execute(query: GetSubmissionCommentsQuery): List<TaskSubmissionCommentDto>
}
