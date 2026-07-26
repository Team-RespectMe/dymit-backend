package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveTasksByCanceledScheduleCommand

/**
 * 취소된 일정 연관 과제 삭제 유즈케이스입니다.
 */
interface RemoveTasksByCanceledScheduleUseCase {

    /**
     * 취소된 일정에 연결된 사전 과제를 삭제합니다.
     *
     * @param command 취소 일정 연관 과제 삭제 명령
     */
    fun execute(command: RemoveTasksByCanceledScheduleCommand)
}
