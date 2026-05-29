package net.noti_me.dymit.dymit_backend_api.application.task.usecases

/**
 * 사전 과제 대상자 제거 유즈케이스입니다.
 */
interface RemoveAssigneeFromPreTasksUseCase {

    /**
     * 사전 과제 대상자를 제거합니다.
     *
     * @param scheduleId 일정 ID
     * @param memberId 회원 ID
     */
    fun removeAssigneeFromPreTasks(scheduleId: String, memberId: String)
}
