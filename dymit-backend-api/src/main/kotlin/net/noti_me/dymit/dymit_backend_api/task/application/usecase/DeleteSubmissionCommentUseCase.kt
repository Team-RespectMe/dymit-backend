package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.DeleteSubmissionCommentCommand

/**
 * 과제 제출 댓글 삭제 유즈케이스입니다.
 */
interface DeleteSubmissionCommentUseCase {

    /**
     * 과제 제출 댓글을 삭제합니다.
     *
     * @param command 댓글 삭제 명령
     */
    fun execute(command: DeleteSubmissionCommentCommand)
}
