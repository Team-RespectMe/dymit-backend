package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.SyncParticipatedScheduleTasksUseCase
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import org.springframework.stereotype.Service

/**
 * 일정 참여 시 사전 과제 대상자를 동기화하는 유즈케이스 구현체입니다.
 */
@Service
class SyncParticipatedScheduleTasksUseCaseImpl(
    private val support: TaskServiceSupport
) : SyncParticipatedScheduleTasksUseCase {

    /**
     * 일정 참여자를 사전 과제 대상자에 반영하고 실제로 추가된 과제를 반환합니다.
     *
     * @param scheduleId 일정 ID
     * @param memberId 신규 참여 멤버 ID
     * @return 실제로 assignee가 추가된 사전 과제 목록
     */
    override fun syncParticipatedScheduleTasks(scheduleId: String, memberId: String): List<Task> {
        val scheduleObjectId = TaskUseCaseObjectIdParser.parse(scheduleId, "scheduleId")
        val memberObjectId = TaskUseCaseObjectIdParser.parse(memberId, "memberId")

        return support.loadTasksBySchedule(scheduleObjectId, TaskType.PRE)
            .filter { task -> support.addAssigneeIfAbsent(task.id!!, memberObjectId) }
    }
}
