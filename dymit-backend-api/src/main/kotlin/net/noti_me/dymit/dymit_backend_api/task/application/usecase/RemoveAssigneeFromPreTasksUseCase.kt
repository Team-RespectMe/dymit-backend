package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveAssigneeFromPreTasksCommand

/**
 * 사전 과제 대상자 제거 유즈케이스입니다.
 */
interface RemoveAssigneeFromPreTasksUseCase {

    /**
     * 사전 과제 대상자를 제거합니다.
     *
     * @param command 사전 과제 대상자 제거 명령
     */
    fun execute(command: RemoveAssigneeFromPreTasksCommand)
}
