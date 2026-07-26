package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetGroupTasksQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto

/**
 * 그룹 과제 목록 조회 유즈케이스입니다.
 */
interface GetGroupTasksUseCase {

    /**
     * 스터디 그룹의 과제 목록을 조회합니다.
     *
     * @param query 그룹 과제 목록 조회 입력
     * @return 과제 DTO 목록
     */
    fun execute(query: GetGroupTasksQuery): List<TaskDto>
}
