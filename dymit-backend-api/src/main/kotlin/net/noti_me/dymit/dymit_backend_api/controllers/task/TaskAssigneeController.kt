package net.noti_me.dymit.dymit_backend_api.controllers.task

import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.application.task.TaskService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskAssigneeResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 과제 제출 대상 조회 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/tasks")
class TaskAssigneeController(
    private val taskService: TaskService
) : TaskAssigneeApi {

    /**
     * 과제에 등록된 제출 대상 목록을 조회합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param taskId 과제 ID
     * @return 과제 제출 대상 목록 응답
     */
    @GetMapping("/{taskId}/assignees")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getTaskAssignees(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable taskId: String
    ): ListResponse<TaskAssigneeResponse> {
        return ListResponse.from(
            taskService.getTaskAssignees(memberInfo, taskId).map { TaskAssigneeResponse.from(it) }
        )
    }
}
