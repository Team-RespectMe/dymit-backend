package net.noti_me.dymit.dymit_backend_api.task.application.usecase

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskAssigneesQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeDto

/**
 * 과제 제출 대상 목록 조회 유즈케이스입니다.
 */
interface GetTaskAssigneesUseCase {

    /**
     * 과제에 등록된 제출 대상 목록을 조회합니다.
     *
     * @param query 과제 대상자 조회 입력
     * @return 과제 제출 대상 DTO 목록
     */
    fun execute(query: GetTaskAssigneesQuery): List<TaskAssigneeDto>
}
