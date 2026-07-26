package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateSubmissionInput

/**
 * 과제 제출 수정 유즈케이스입니다.
 */
interface UpdateSubmissionUseCase {

    /**
     * 과제 제출을 수정합니다.
     *
     * @param input 제출 수정 입력
     * @return 수정된 제출 DTO
     */
    fun execute(input: UpdateSubmissionInput): TaskSubmissionDto
}
