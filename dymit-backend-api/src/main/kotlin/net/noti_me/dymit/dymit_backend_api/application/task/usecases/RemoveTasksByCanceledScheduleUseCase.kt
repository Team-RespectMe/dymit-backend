package net.noti_me.dymit.dymit_backend_api.application.task.usecases

/**
 * 취소된 일정 연관 과제 삭제 유즈케이스입니다.
 */
interface RemoveTasksByCanceledScheduleUseCase {

    /**
     * 취소된 일정에 연결된 사전 과제를 삭제합니다.
     *
     * @param scheduleId 일정 ID
     * @param groupId 스터디 그룹 ID
     */
    fun removeTasksByCanceledSchedule(scheduleId: String, groupId: String)
}
