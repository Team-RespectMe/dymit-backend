package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.WithdrawSubmissionCommand

/**
 * 과제 제출 철회 유즈케이스입니다.
 */
interface WithdrawSubmissionUseCase {

    /**
     * 과제 제출을 철회합니다.
     *
     * @param command 과제 제출 철회 명령
     */
    fun execute(command: WithdrawSubmissionCommand)
}
