package net.noti_me.dymit.dymit_backend_api.application.task.impl

import net.noti_me.dymit.dymit_backend_api.application.task.TaskService
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
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.AddAssigneeToPreTasksUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateSubmissionCommentUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.CreateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.DeleteSubmissionCommentUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.GetGroupTasksUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.GetSubmissionCommentsUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.GetTaskAssigneesUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.GetTaskDetailUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.GetTaskSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.GetTaskSubmissionsUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.RemoveAssigneeFromPreTasksUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.RemoveTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.RemoveTasksByCanceledScheduleUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateSubmissionCommentUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.UpdateTaskUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl.WithdrawSubmissionUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.springframework.context.ApplicationEventPublisher

/**
 * 기존 테스트 호환성을 위한 과제 서비스 구현체입니다.
 *
 * 실제 비즈니스 로직은 유즈케이스 구현체에 있으며,
 * 해당 클래스는 TaskService 위임만 수행합니다.
 */
class TaskServiceImpl(
    private val delegate: TaskService
) : TaskService {

    @Suppress("UNUSED_PARAMETER")
    constructor(
        support: TaskServiceSupport,
        taskSubmissionRepository: TaskSubmissionRepository,
        taskSubmissionCommentRepository: TaskSubmissionCommentRepository,
        eventPublisher: ApplicationEventPublisher
    ) : this(buildDelegate(support, taskSubmissionRepository, eventPublisher))

    override fun createTask(memberInfo: MemberInfo, groupId: String, command: CreateTaskCommand): TaskDto {
        return delegate.createTask(memberInfo, groupId, command)
    }

    override fun updateTask(memberInfo: MemberInfo, groupId: String, taskId: String, command: UpdateTaskCommand): TaskDto {
        return delegate.updateTask(memberInfo, groupId, taskId, command)
    }

    override fun removeTask(memberInfo: MemberInfo, groupId: String, taskId: String) {
        delegate.removeTask(memberInfo, groupId, taskId)
    }

    override fun getGroupTasks(memberInfo: MemberInfo, groupId: String): List<TaskDto> {
        return delegate.getGroupTasks(memberInfo, groupId)
    }

    override fun getTaskDetail(memberInfo: MemberInfo, groupId: String, taskId: String): TaskDto {
        return delegate.getTaskDetail(memberInfo, groupId, taskId)
    }

    override fun createSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        command: CreateTaskSubmissionCommand
    ): TaskSubmissionDto {
        return delegate.createSubmission(memberInfo, groupId, taskId, command)
    }

    override fun updateSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: UpdateTaskSubmissionCommand
    ): TaskSubmissionDto {
        return delegate.updateSubmission(memberInfo, groupId, taskId, submissionId, command)
    }

    override fun withdrawSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String) {
        delegate.withdrawSubmission(memberInfo, groupId, taskId, submissionId)
    }

    override fun getTaskSubmissions(memberInfo: MemberInfo, groupId: String, taskId: String): List<TaskSubmissionDto> {
        return delegate.getTaskSubmissions(memberInfo, groupId, taskId)
    }

    override fun getTaskSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        memberId: String
    ): TaskSubmissionDto {
        return delegate.getTaskSubmission(memberInfo, groupId, taskId, memberId)
    }

    override fun createSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: CreateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto {
        return delegate.createSubmissionComment(memberInfo, groupId, taskId, submissionId, command)
    }

    override fun updateSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String,
        command: UpdateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto {
        return delegate.updateSubmissionComment(memberInfo, groupId, taskId, submissionId, commentId, command)
    }

    override fun deleteSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String
    ) {
        delegate.deleteSubmissionComment(memberInfo, groupId, taskId, submissionId, commentId)
    }

    override fun getSubmissionComments(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    ): List<TaskSubmissionCommentDto> {
        return delegate.getSubmissionComments(memberInfo, groupId, taskId, submissionId)
    }

    override fun getTaskAssignees(memberInfo: MemberInfo, taskId: String): List<TaskAssigneeDto> {
        return delegate.getTaskAssignees(memberInfo, taskId)
    }

    override fun addAssigneeToPreTasks(scheduleId: String, memberId: String) {
        delegate.addAssigneeToPreTasks(scheduleId, memberId)
    }

    override fun removeAssigneeFromPreTasks(scheduleId: String, memberId: String) {
        delegate.removeAssigneeFromPreTasks(scheduleId, memberId)
    }

    override fun removeTasksByCanceledSchedule(scheduleId: String, groupId: String) {
        delegate.removeTasksByCanceledSchedule(scheduleId, groupId)
    }

    companion object {

        private fun buildDelegate(
            support: TaskServiceSupport,
            taskSubmissionRepository: TaskSubmissionRepository,
            eventPublisher: ApplicationEventPublisher
        ): TaskService {
            val taskDeletionSupport = TaskDeletionSupport(support, eventPublisher)
            return TaskServiceFacade(
                createTaskUseCase = CreateTaskUseCaseImpl(support, eventPublisher),
                updateTaskUseCase = UpdateTaskUseCaseImpl(support, eventPublisher),
                removeTaskUseCase = RemoveTaskUseCaseImpl(support, taskDeletionSupport),
                getGroupTasksUseCase = GetGroupTasksUseCaseImpl(support),
                getTaskDetailUseCase = GetTaskDetailUseCaseImpl(support),
                getTaskAssigneesUseCase = GetTaskAssigneesUseCaseImpl(support),
                createSubmissionUseCase = CreateSubmissionUseCaseImpl(
                    support,
                    taskSubmissionRepository,
                    eventPublisher
                ),
                updateSubmissionUseCase = UpdateSubmissionUseCaseImpl(support),
                withdrawSubmissionUseCase = WithdrawSubmissionUseCaseImpl(support),
                getTaskSubmissionsUseCase = GetTaskSubmissionsUseCaseImpl(support),
                getTaskSubmissionUseCase = GetTaskSubmissionUseCaseImpl(support),
                createSubmissionCommentUseCase = CreateSubmissionCommentUseCaseImpl(support, eventPublisher),
                updateSubmissionCommentUseCase = UpdateSubmissionCommentUseCaseImpl(support),
                deleteSubmissionCommentUseCase = DeleteSubmissionCommentUseCaseImpl(support),
                getSubmissionCommentsUseCase = GetSubmissionCommentsUseCaseImpl(support),
                addAssigneeToPreTasksUseCase = AddAssigneeToPreTasksUseCaseImpl(support),
                removeAssigneeFromPreTasksUseCase = RemoveAssigneeFromPreTasksUseCaseImpl(support),
                removeTasksByCanceledScheduleUseCase = RemoveTasksByCanceledScheduleUseCaseImpl(
                    support,
                    taskDeletionSupport
                )
            )
        }
    }
}
