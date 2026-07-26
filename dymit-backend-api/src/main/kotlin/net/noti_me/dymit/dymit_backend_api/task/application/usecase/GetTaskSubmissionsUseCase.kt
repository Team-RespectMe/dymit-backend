package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskSubmissionsQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto

/**
 * 과제 제출 목록 조회 유즈케이스입니다.
 */
interface GetTaskSubmissionsUseCase {

    /**
     * 과제 제출 목록을 조회합니다.
     *
     * @param query 과제 제출 목록 조회 입력
     * @return 제출 DTO 목록
     */
    fun execute(query: GetTaskSubmissionsQuery): List<TaskSubmissionDto>
}
