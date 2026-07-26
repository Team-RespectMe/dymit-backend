package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskSubmissionQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto

/**
 * 과제 제출 단건 조회 유즈케이스입니다.
 */
interface GetTaskSubmissionUseCase {

    /**
     * 과제 제출 단건을 조회합니다.
     *
     * @param query 과제 제출 단건 조회 입력
     * @return 제출 DTO
     */
    fun execute(query: GetTaskSubmissionQuery): TaskSubmissionDto
}
