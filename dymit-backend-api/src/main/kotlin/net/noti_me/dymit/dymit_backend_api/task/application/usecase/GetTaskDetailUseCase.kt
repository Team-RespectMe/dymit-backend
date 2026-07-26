package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskDetailQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto

/**
 * 과제 상세 조회 유즈케이스입니다.
 */
interface GetTaskDetailUseCase {

    /**
     * 과제 상세를 조회합니다.
     *
     * @param query 과제 상세 조회 입력
     * @return 과제 DTO
     */
    fun execute(query: GetTaskDetailQuery): TaskDto
}
