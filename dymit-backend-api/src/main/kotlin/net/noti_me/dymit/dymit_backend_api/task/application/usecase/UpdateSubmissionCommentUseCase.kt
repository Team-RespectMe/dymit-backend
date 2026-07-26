package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateSubmissionCommentInput

/**
 * 과제 제출 댓글 수정 유즈케이스입니다.
 */
interface UpdateSubmissionCommentUseCase {

    /**
     * 과제 제출 댓글을 수정합니다.
     *
     * @param input 댓글 수정 입력
     * @return 수정된 댓글 DTO
     */
    fun execute(input: UpdateSubmissionCommentInput): TaskSubmissionCommentDto
}
