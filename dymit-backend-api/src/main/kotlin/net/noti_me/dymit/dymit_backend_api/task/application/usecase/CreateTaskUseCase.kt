package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto

/**
 * 과제 생성 유즈케이스입니다.
 */
interface CreateTaskUseCase {

    /**
     * 과제를 생성합니다.
     *
     * @param input 과제 생성 입력
     * @return 생성된 과제 DTO
     */
    fun execute(input: CreateTaskInput): TaskDto
}
