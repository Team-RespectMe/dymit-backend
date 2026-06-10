package net.noti_me.dymit.dymit_backend_api.controllers.task

import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.task.TaskService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskResponse
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommentCommandRequest
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionCommentResponse
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskSubmissionResponse
import net.noti_me.dymit.dymit_backend_api.controllers.task.dto.TaskUpdateRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/study-groups")
class TaskController(
    private val taskService: TaskService
) : TaskApi {

    @PostMapping("/{groupId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun createTask(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid @Sanitize request: TaskCommandRequest
    ): TaskResponse {
        return TaskResponse.from(taskService.createTask(memberInfo, groupId, request.toCreateCommand()), groupId)
    }

    @PutMapping("/{groupId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updateTask(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @RequestBody @Valid @Sanitize request: TaskUpdateRequest
    ): TaskResponse {
        return TaskResponse.from(taskService.updateTask(memberInfo, groupId, taskId, request.toCommand()), groupId)
    }

    @DeleteMapping("/{groupId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun deleteTask(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String
    ) {
        taskService.removeTask(memberInfo, groupId, taskId)
    }

    @GetMapping("/{groupId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getTasks(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): ListResponse<TaskResponse> {
        return ListResponse.from(taskService.getGroupTasks(memberInfo, groupId).map { TaskResponse.from(it, groupId) })
    }

    @GetMapping("/{groupId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getTask(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String
    ): TaskResponse {
        return TaskResponse.from(taskService.getTaskDetail(memberInfo, groupId, taskId), groupId)
    }

    @PostMapping("/{groupId}/tasks/{taskId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun createSubmission(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @RequestBody @Valid @Sanitize request: TaskSubmissionCommandRequest
    ): TaskSubmissionResponse {
        return TaskSubmissionResponse.from(taskService.createSubmission(memberInfo, groupId, taskId, request.toCreateCommand()))
    }

    @PutMapping("/{groupId}/tasks/{taskId}/submissions/{submissionId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updateSubmission(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @PathVariable submissionId: String,
        @RequestBody @Valid @Sanitize request: TaskSubmissionCommandRequest
    ): TaskSubmissionResponse {
        return TaskSubmissionResponse.from(
            taskService.updateSubmission(
                memberInfo,
                groupId,
                taskId,
                submissionId,
                request.toUpdateCommand()
            )
        )
    }

    @DeleteMapping("/{groupId}/tasks/{taskId}/submissions/{submissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun withdrawSubmission(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @PathVariable submissionId: String
    ) {
        taskService.withdrawSubmission(memberInfo, groupId, taskId, submissionId)
    }

    @DeleteMapping("/{groupId}/tasks/{taskId}/submissions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun withdrawCheckSubmissionByAssignee(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @RequestParam assigneeId: String
    ) {
        taskService.withdrawCheckSubmissionByAssignee(memberInfo, groupId, taskId, assigneeId)
    }

    @GetMapping("/{groupId}/tasks/{taskId}/submissions")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getSubmission(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @RequestParam assigneeId: String
    ): TaskSubmissionResponse {
        return TaskSubmissionResponse.from(
            taskService.getTaskSubmission(memberInfo, groupId, taskId, assigneeId)
        )
    }

    // 요청에 따라 제출 목록 조회 엔드포인트 노출을 중단합니다.
    // @GetMapping("/{groupId}/tasks/{taskId}/submissions")
    // @ResponseStatus(HttpStatus.OK)
    // @RolesAllowed("MEMBER", "ADMIN")
    // override fun getSubmissions(
    //     @LoginMember memberInfo: MemberInfo,
    //     @PathVariable groupId: String,
    //     @PathVariable taskId: String
    // ): ListResponse<TaskSubmissionResponse> {
    //     return ListResponse.from(
    //         taskService.getTaskSubmissions(memberInfo, groupId, taskId)
    //             .map { TaskSubmissionResponse.from(it) }
    //     )
    // }

    @PostMapping("/{groupId}/tasks/{taskId}/submissions/{submissionId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun createSubmissionComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @PathVariable submissionId: String,
        @RequestBody @Valid @Sanitize request: TaskSubmissionCommentCommandRequest
    ): TaskSubmissionCommentResponse {
        return TaskSubmissionCommentResponse.from(
            taskService.createSubmissionComment(
                memberInfo,
                groupId,
                taskId,
                submissionId,
                request.toCreateCommand()
            )
        )
    }

    @PutMapping("/{groupId}/tasks/{taskId}/submissions/{submissionId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun updateSubmissionComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @PathVariable submissionId: String,
        @PathVariable commentId: String,
        @RequestBody @Valid @Sanitize request: TaskSubmissionCommentCommandRequest
    ): TaskSubmissionCommentResponse {
        return TaskSubmissionCommentResponse.from(
            taskService.updateSubmissionComment(
                memberInfo,
                groupId,
                taskId,
                submissionId,
                commentId,
                request.toUpdateCommand()
            )
        )
    }

    @DeleteMapping("/{groupId}/tasks/{taskId}/submissions/{submissionId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun deleteSubmissionComment(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @PathVariable submissionId: String,
        @PathVariable commentId: String
    ) {
        taskService.deleteSubmissionComment(memberInfo, groupId, taskId, submissionId, commentId)
    }

    @GetMapping("/{groupId}/tasks/{taskId}/submissions/{submissionId}/comments")
    @ResponseStatus(HttpStatus.OK)
    @RolesAllowed("MEMBER", "ADMIN")
    override fun getSubmissionComments(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable taskId: String,
        @PathVariable submissionId: String
    ): ListResponse<TaskSubmissionCommentResponse> {
        return ListResponse.from(
            taskService.getSubmissionComments(memberInfo, groupId, taskId, submissionId)
                .map { TaskSubmissionCommentResponse.from(it) }
        )
    }
}
