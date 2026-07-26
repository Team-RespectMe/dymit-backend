package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.AddAssigneeToPreTasksCommand

/**
 * 사전 과제 대상자 추가 유즈케이스입니다.
 */
interface AddAssigneeToPreTasksUseCase {

    /**
     * 사전 과제 대상자를 추가합니다.
     *
     * @param command 사전 과제 대상자 추가 명령
     */
    fun execute(command: AddAssigneeToPreTasksCommand)
}
