package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskService
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.AddAssigneeToPreTasksCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateSubmissionCommentInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateSubmissionInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.DeleteSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetGroupTasksQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetSubmissionCommentsQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskAssigneesQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskDetailQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskSubmissionQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskSubmissionsQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveAssigneeFromPreTasksCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.RemoveTasksByCanceledScheduleCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.SyncParticipatedScheduleTasksCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.SyncedTaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateSubmissionCommentInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateSubmissionInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.UpdateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.WithdrawCheckSubmissionByAssigneeCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.WithdrawSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.AddAssigneeToPreTasksUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.CreateSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.CreateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.CreateTaskUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.DeleteSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetGroupTasksUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetSubmissionCommentsUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetTaskAssigneesUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetTaskDetailUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetTaskSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetTaskSubmissionsUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.RemoveAssigneeFromPreTasksUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.RemoveTaskUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.RemoveTasksByCanceledScheduleUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.SyncParticipatedScheduleTasksUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.UpdateSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.UpdateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.UpdateTaskUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.WithdrawCheckSubmissionByAssigneeUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.WithdrawSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 과제 서비스 파사드입니다.
 */
@Service
class TaskServiceFacade(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val removeTaskUseCase: RemoveTaskUseCase,
    private val getGroupTasksUseCase: GetGroupTasksUseCase,
    private val getTaskDetailUseCase: GetTaskDetailUseCase,
    private val getTaskAssigneesUseCase: GetTaskAssigneesUseCase,
    private val createSubmissionUseCase: CreateSubmissionUseCase,
    private val updateSubmissionUseCase: UpdateSubmissionUseCase,
    private val withdrawSubmissionUseCase: WithdrawSubmissionUseCase,
    private val withdrawCheckSubmissionByAssigneeUseCase: WithdrawCheckSubmissionByAssigneeUseCase,
    private val getTaskSubmissionsUseCase: GetTaskSubmissionsUseCase,
    private val getTaskSubmissionUseCase: GetTaskSubmissionUseCase,
    private val createSubmissionCommentUseCase: CreateSubmissionCommentUseCase,
    private val updateSubmissionCommentUseCase: UpdateSubmissionCommentUseCase,
    private val deleteSubmissionCommentUseCase: DeleteSubmissionCommentUseCase,
    private val getSubmissionCommentsUseCase: GetSubmissionCommentsUseCase,
    private val addAssigneeToPreTasksUseCase: AddAssigneeToPreTasksUseCase,
    private val syncParticipatedScheduleTasksUseCase: SyncParticipatedScheduleTasksUseCase,
    private val removeAssigneeFromPreTasksUseCase: RemoveAssigneeFromPreTasksUseCase,
    private val removeTasksByCanceledScheduleUseCase: RemoveTasksByCanceledScheduleUseCase
) : TaskService {

    override fun createTask(memberInfo: MemberInfo, groupId: String, command: CreateTaskCommand): TaskDto {
        return createTaskUseCase.execute(CreateTaskInput(memberInfo, groupId, command))
    }

    override fun updateTask(memberInfo: MemberInfo, groupId: String, taskId: String, command: UpdateTaskCommand): TaskDto {
        return updateTaskUseCase.execute(UpdateTaskInput(memberInfo, groupId, taskId, command))
    }

    override fun removeTask(memberInfo: MemberInfo, groupId: String, taskId: String) {
        removeTaskUseCase.execute(RemoveTaskCommand(memberInfo, groupId, taskId))
    }

    override fun getGroupTasks(memberInfo: MemberInfo, groupId: String): List<TaskDto> {
        return getGroupTasksUseCase.execute(GetGroupTasksQuery(memberInfo, groupId))
    }

    override fun getTaskDetail(memberInfo: MemberInfo, groupId: String, taskId: String): TaskDto {
        return getTaskDetailUseCase.execute(GetTaskDetailQuery(memberInfo, groupId, taskId))
    }

    override fun createSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        command: CreateTaskSubmissionCommand
    ): TaskSubmissionDto {
        return createSubmissionUseCase.execute(CreateSubmissionInput(memberInfo, groupId, taskId, command))
    }

    override fun updateSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: UpdateTaskSubmissionCommand
    ): TaskSubmissionDto {
        return updateSubmissionUseCase.execute(
            UpdateSubmissionInput(memberInfo, groupId, taskId, submissionId, command)
        )
    }

    override fun withdrawSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String) {
        withdrawSubmissionUseCase.execute(WithdrawSubmissionCommand(memberInfo, groupId, taskId, submissionId))
    }

    override fun withdrawCheckSubmissionByAssignee(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        assigneeId: String
    ) {
        withdrawCheckSubmissionByAssigneeUseCase.execute(
            WithdrawCheckSubmissionByAssigneeCommand(memberInfo, groupId, taskId, assigneeId)
        )
    }

    override fun getTaskSubmissions(memberInfo: MemberInfo, groupId: String, taskId: String): List<TaskSubmissionDto> {
        return getTaskSubmissionsUseCase.execute(GetTaskSubmissionsQuery(memberInfo, groupId, taskId))
    }

    override fun getTaskSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        memberId: String
    ): TaskSubmissionDto {
        return getTaskSubmissionUseCase.execute(GetTaskSubmissionQuery(memberInfo, groupId, taskId, memberId))
    }

    override fun createSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: CreateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto {
        return createSubmissionCommentUseCase.execute(
            CreateSubmissionCommentInput(memberInfo, groupId, taskId, submissionId, command)
        )
    }

    override fun updateSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String,
        command: UpdateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto {
        return updateSubmissionCommentUseCase.execute(
            UpdateSubmissionCommentInput(memberInfo, groupId, taskId, submissionId, commentId, command)
        )
    }

    override fun deleteSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String
    ) {
        deleteSubmissionCommentUseCase.execute(
            DeleteSubmissionCommentCommand(memberInfo, groupId, taskId, submissionId, commentId)
        )
    }

    override fun getSubmissionComments(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    ): List<TaskSubmissionCommentDto> {
        return getSubmissionCommentsUseCase.execute(
            GetSubmissionCommentsQuery(memberInfo, groupId, taskId, submissionId)
        )
    }

    override fun getTaskAssignees(memberInfo: MemberInfo, taskId: String): List<TaskAssigneeDto> {
        return getTaskAssigneesUseCase.execute(GetTaskAssigneesQuery(memberInfo, taskId))
    }

    override fun addAssigneeToPreTasks(scheduleId: String, memberId: String) {
        addAssigneeToPreTasksUseCase.execute(AddAssigneeToPreTasksCommand(scheduleId, memberId))
    }

    override fun syncParticipatedScheduleTasks(scheduleId: String, memberId: String): List<SyncedTaskDto> {
        return syncParticipatedScheduleTasksUseCase.execute(
            SyncParticipatedScheduleTasksCommand(scheduleId, memberId)
        )
    }

    override fun removeAssigneeFromPreTasks(scheduleId: String, memberId: String) {
        removeAssigneeFromPreTasksUseCase.execute(RemoveAssigneeFromPreTasksCommand(scheduleId, memberId))
    }

    override fun removeTasksByCanceledSchedule(scheduleId: String, groupId: String) {
        removeTasksByCanceledScheduleUseCase.execute(RemoveTasksByCanceledScheduleCommand(scheduleId, groupId))
    }
}
