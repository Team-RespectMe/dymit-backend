package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.SyncParticipatedScheduleTasksCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.SyncedTaskDto

/**
 * 일정 참여 시 사전 과제 대상자를 동기화하고 실제 반영된 과제를 반환하는 유즈케이스입니다.
 */
interface SyncParticipatedScheduleTasksUseCase {

    /**
     * 일정 참여자를 사전 과제 대상자에 반영하고 실제로 추가된 과제를 반환합니다.
     *
     * @param command 일정 참여 사전 과제 동기화 명령
     * @return 실제로 assignee가 추가된 사전 과제 목록
     */
    fun execute(command: SyncParticipatedScheduleTasksCommand): List<SyncedTaskDto>
}
