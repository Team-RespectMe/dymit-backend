package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.WithdrawCheckSubmissionByAssigneeCommand

/**
 * 체크형 과제 제출 철회 유즈케이스입니다.
 */
interface WithdrawCheckSubmissionByAssigneeUseCase {

    /**
     * 체크형 과제 제출을 대상자 ID 기준으로 철회합니다.
     *
     * @param command 체크형 과제 제출 철회 명령
     */
    fun execute(command: WithdrawCheckSubmissionByAssigneeCommand)
}
