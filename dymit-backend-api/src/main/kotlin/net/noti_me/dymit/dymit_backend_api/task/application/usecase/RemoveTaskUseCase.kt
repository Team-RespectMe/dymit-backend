package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveTaskCommand

/**
 * 과제 삭제 유즈케이스입니다.
 */
interface RemoveTaskUseCase {

    /**
     * 과제를 삭제합니다.
     *
     * @param command 과제 삭제 명령
     */
    fun execute(command: RemoveTaskCommand)
}
