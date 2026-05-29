package net.noti_me.dymit.dymit_backend_api.application.task.usecases

/**
 * 사전 과제 대상자 추가 유즈케이스입니다.
 */
interface AddAssigneeToPreTasksUseCase {

    /**
     * 사전 과제 대상자를 추가합니다.
     *
     * @param scheduleId 일정 ID
     * @param memberId 회원 ID
     */
    fun addAssigneeToPreTasks(scheduleId: String, memberId: String)
}
