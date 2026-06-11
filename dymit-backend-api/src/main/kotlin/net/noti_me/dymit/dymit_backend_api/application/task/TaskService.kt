package net.noti_me.dymit.dymit_backend_api.application.task

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.task.Task

/**
 * 과제 서비스 인터페이스입니다.
 */
interface TaskService {

    fun createTask(memberInfo: MemberInfo, groupId: String, command: CreateTaskCommand): TaskDto

    fun updateTask(memberInfo: MemberInfo, groupId: String, taskId: String, command: UpdateTaskCommand): TaskDto

    fun removeTask(memberInfo: MemberInfo, groupId: String, taskId: String)

    fun getGroupTasks(memberInfo: MemberInfo, groupId: String): List<TaskDto>

    fun getTaskDetail(memberInfo: MemberInfo, groupId: String, taskId: String): TaskDto

    fun createSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        command: CreateTaskSubmissionCommand
    ): TaskSubmissionDto

    fun updateSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: UpdateTaskSubmissionCommand
    ): TaskSubmissionDto

    fun withdrawSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String)

    fun withdrawCheckSubmissionByAssignee(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        assigneeId: String
    )

    fun getTaskSubmissions(memberInfo: MemberInfo, groupId: String, taskId: String): List<TaskSubmissionDto>

    fun getTaskSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        memberId: String
    ): TaskSubmissionDto

    fun createSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: CreateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto

    fun updateSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String,
        command: UpdateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto

    fun deleteSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String
    )

    fun getSubmissionComments(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    ): List<TaskSubmissionCommentDto>

    fun getTaskAssignees(memberInfo: MemberInfo, taskId: String): List<TaskAssigneeDto>

    fun addAssigneeToPreTasks(scheduleId: String, memberId: String)

    fun syncParticipatedScheduleTasks(scheduleId: String, memberId: String): List<Task>

    fun removeAssigneeFromPreTasks(scheduleId: String, memberId: String)

    fun removeTasksByCanceledSchedule(scheduleId: String, groupId: String)
}
