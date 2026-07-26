package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.SyncParticipatedScheduleTasksCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.SyncedTaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.SyncParticipatedScheduleTasksUseCase
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
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
     * @param command 일정 참여 사전 과제 동기화 명령
     * @return 실제로 assignee가 추가된 사전 과제 목록
     */
    override fun execute(command: SyncParticipatedScheduleTasksCommand): List<SyncedTaskDto> {
        val (scheduleId, memberId) = command
        val scheduleObjectId = TaskUseCaseObjectIdParser.parse(scheduleId, "scheduleId")
        val memberObjectId = TaskUseCaseObjectIdParser.parse(memberId, "memberId")

        return support.loadTasksBySchedule(scheduleObjectId, TaskType.PRE)
            .filter { task -> support.addAssigneeIfAbsent(task.id!!, memberObjectId) }
            .map { task -> SyncedTaskDto(taskId = task.identifier) }
    }
}
