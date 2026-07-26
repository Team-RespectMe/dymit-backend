package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskInput

/**
 * 과제 수정 유즈케이스입니다.
 */
interface UpdateTaskUseCase {

    /**
     * 과제를 수정합니다.
     *
     * @param input 과제 수정 입력
     * @return 수정된 과제 DTO
     */
    fun execute(input: UpdateTaskInput): TaskDto
}
