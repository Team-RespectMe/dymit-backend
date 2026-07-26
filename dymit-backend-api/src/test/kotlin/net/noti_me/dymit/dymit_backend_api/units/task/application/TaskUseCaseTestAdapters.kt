package net.noti_me.dymit.dymit_backend_api.units.task.application

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.*
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.*

/** Test-only adapters that exercise Task use cases through their execute input boundary. */
internal fun CreateTaskUseCase.createTask(memberInfo: MemberInfo, groupId: String, command: CreateTaskCommand) = execute(CreateTaskInput(memberInfo, groupId, command))
internal fun UpdateTaskUseCase.updateTask(memberInfo: MemberInfo, groupId: String, taskId: String, command: UpdateTaskCommand) = execute(UpdateTaskInput(memberInfo, groupId, taskId, command))
internal fun RemoveTaskUseCase.removeTask(memberInfo: MemberInfo, groupId: String, taskId: String) = execute(RemoveTaskCommand(memberInfo, groupId, taskId))
internal fun CreateSubmissionUseCase.createSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, command: CreateTaskSubmissionCommand) = execute(CreateSubmissionInput(memberInfo, groupId, taskId, command))
internal fun UpdateSubmissionUseCase.updateSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String, command: UpdateTaskSubmissionCommand) = execute(UpdateSubmissionInput(memberInfo, groupId, taskId, submissionId, command))
internal fun WithdrawSubmissionUseCase.withdrawSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String) = execute(WithdrawSubmissionCommand(memberInfo, groupId, taskId, submissionId))
internal fun WithdrawCheckSubmissionByAssigneeUseCase.withdrawCheckSubmissionByAssignee(memberInfo: MemberInfo, groupId: String, taskId: String, assigneeId: String) = execute(WithdrawCheckSubmissionByAssigneeCommand(memberInfo, groupId, taskId, assigneeId))
internal fun CreateSubmissionCommentUseCase.createSubmissionComment(memberInfo: MemberInfo, groupId: String, taskId: String, submissionId: String, command: CreateTaskSubmissionCommentCommand) = execute(CreateSubmissionCommentInput(memberInfo, groupId, taskId, submissionId, command))
internal fun GetTaskAssigneesUseCase.getTaskAssignees(memberInfo: MemberInfo, taskId: String) = execute(GetTaskAssigneesQuery(memberInfo, taskId))
internal fun GetTaskSubmissionUseCase.getTaskSubmission(memberInfo: MemberInfo, groupId: String, taskId: String, memberId: String) = execute(GetTaskSubmissionQuery(memberInfo, groupId, taskId, memberId))
internal fun SyncParticipatedScheduleTasksUseCase.syncParticipatedScheduleTasks(scheduleId: String, memberId: String) = execute(SyncParticipatedScheduleTasksCommand(scheduleId, memberId))
internal fun RemoveTasksByCanceledScheduleUseCase.removeTasksByCanceledSchedule(scheduleId: String, groupId: String) = execute(RemoveTasksByCanceledScheduleCommand(scheduleId, groupId))
