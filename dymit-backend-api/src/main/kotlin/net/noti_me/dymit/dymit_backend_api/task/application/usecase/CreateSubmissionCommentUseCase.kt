package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateSubmissionCommentInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionCommentDto

/**
 * 과제 제출 댓글 생성 유즈케이스입니다.
 */
interface CreateSubmissionCommentUseCase {

    /**
     * 과제 제출 댓글을 생성합니다.
     *
     * @param input 댓글 생성 입력
     * @return 생성된 댓글 DTO
     */
    fun execute(input: CreateSubmissionCommentInput): TaskSubmissionCommentDto
}
