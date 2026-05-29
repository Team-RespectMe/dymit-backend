package net.noti_me.dymit.dymit_backend_api.application.task.impl

import net.noti_me.dymit.dymit_backend_api.application.task.TaskService
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.AddAssigneeToPreTasksUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateTaskUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.DeleteSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetGroupTasksUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetSubmissionCommentsUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetTaskDetailUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetTaskSubmissionsUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.RemoveAssigneeFromPreTasksUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.RemoveTaskUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.RemoveTasksByCanceledScheduleUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.UpdateSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.UpdateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.UpdateTaskUseCase
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.WithdrawSubmissionUseCase
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
    private val createSubmissionUseCase: CreateSubmissionUseCase,
    private val updateSubmissionUseCase: UpdateSubmissionUseCase,
    private val withdrawSubmissionUseCase: WithdrawSubmissionUseCase,
    private val getTaskSubmissionsUseCase: GetTaskSubmissionsUseCase,
    private val createSubmissionCommentUseCase: CreateSubmissionCommentUseCase,
    private val updateSubmissionCommentUseCase: UpdateSubmissionCommentUseCase,
    private val deleteSubmissionCommentUseCase: DeleteSubmissionCommentUseCase,
    private val getSubmissionCommentsUseCase: GetSubmissionCommentsUseCase,
    private val addAssigneeToPreTasksUseCase: AddAssigneeToPreTasksUseCase,
    private val removeAssigneeFromPreTasksUseCase: RemoveAssigneeFromPreTasksUseCase,
    private val removeTasksByCanceledScheduleUseCase: RemoveTasksByCanceledScheduleUseCase
) : TaskService {

    override fun createTask(memberInfo: MemberInfo, groupId: String, command: CreateTaskCommand): TaskDto {
        return createTaskUseCase.createTask(memberInfo, groupId, command)
    }

    override fun updateTask(memberInfo: MemberInfo, groupId: String, taskId: String, command: UpdateTaskCommand): TaskDto {
        return updateTaskUseCase.updateTask(memberInfo, groupId, taskId, command)
    }

    override fun removeTask(memberInfo: MemberInfo, groupId: String, taskId: String) {
        removeTaskUseCase.removeTask(memberInfo, groupId, taskId)
    }

    override fun getGroupTasks(memberInfo: MemberInfo, groupId: String): List<TaskDto> {
        return getGroupTasksUseCase.getGroupTasks(memberInfo, groupId)
    }

    override fun getTaskDetail(memberInfo: MemberInfo, groupId: String, taskId: String): TaskDto {
        return getTaskDetailUseCase.getTaskDetail(memberInfo, groupId, taskId)
    }

    override fun createSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        command: CreateTaskSubmissionCommand
    ): TaskSubmissionDto {
        return createSubmissionUseCase.createSubmission(memberInfo, groupId, taskId, command)
    }

    override fun updateSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: UpdateTaskSubmissionCommand
    ): TaskSubmissionDto {
        return updateSubmissionUseCase.updateSubmission(memberInfo, groupId, taskId, submissionId, command)
    }

    override fun withdrawSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String) {
        withdrawSubmissionUseCase.withdrawSubmission(memberInfo, groupId, taskId, submissionId)
    }

    override fun getTaskSubmissions(memberInfo: MemberInfo, groupId: String, taskId: String): List<TaskSubmissionDto> {
        return getTaskSubmissionsUseCase.getTaskSubmissions(memberInfo, groupId, taskId)
    }

    override fun createSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: CreateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto {
        return createSubmissionCommentUseCase.createSubmissionComment(memberInfo, groupId, taskId, submissionId, command)
    }

    override fun updateSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String,
        command: UpdateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto {
        return updateSubmissionCommentUseCase.updateSubmissionComment(
            memberInfo,
            groupId,
            taskId,
            submissionId,
            commentId,
            command
        )
    }

    override fun deleteSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String
    ) {
        deleteSubmissionCommentUseCase.deleteSubmissionComment(memberInfo, groupId, taskId, submissionId, commentId)
    }

    override fun getSubmissionComments(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    ): List<TaskSubmissionCommentDto> {
        return getSubmissionCommentsUseCase.getSubmissionComments(memberInfo, groupId, taskId, submissionId)
    }

    override fun addAssigneeToPreTasks(scheduleId: String, memberId: String) {
        addAssigneeToPreTasksUseCase.addAssigneeToPreTasks(scheduleId, memberId)
    }

    override fun removeAssigneeFromPreTasks(scheduleId: String, memberId: String) {
        removeAssigneeFromPreTasksUseCase.removeAssigneeFromPreTasks(scheduleId, memberId)
    }

    override fun removeTasksByCanceledSchedule(scheduleId: String, groupId: String) {
        removeTasksByCanceledScheduleUseCase.removeTasksByCanceledSchedule(scheduleId, groupId)
    }
}
