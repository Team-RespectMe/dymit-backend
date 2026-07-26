package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateSubmissionInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto

/**
 * 과제 제출 생성 유즈케이스입니다.
 */
interface CreateSubmissionUseCase {

    /**
     * 과제 제출을 생성합니다.
     *
     * @param input 제출 생성 입력
     * @return 생성된 제출 DTO
     */
    fun execute(input: CreateSubmissionInput): TaskSubmissionDto
}
