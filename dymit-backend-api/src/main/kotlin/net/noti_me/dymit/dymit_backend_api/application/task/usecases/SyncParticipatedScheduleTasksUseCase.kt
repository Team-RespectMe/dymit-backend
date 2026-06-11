package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.domain.task.Task

/**
 * 일정 참여 시 사전 과제 대상자를 동기화하고 실제 반영된 과제를 반환하는 유즈케이스입니다.
 */
interface SyncParticipatedScheduleTasksUseCase {

    /**
     * 일정 참여자를 사전 과제 대상자에 반영하고 실제로 추가된 과제를 반환합니다.
     *
     * @param scheduleId 일정 ID
     * @param memberId 신규 참여 멤버 ID
     * @return 실제로 assignee가 추가된 사전 과제 목록
     */
    fun syncParticipatedScheduleTasks(scheduleId: String, memberId: String): List<Task>
}
