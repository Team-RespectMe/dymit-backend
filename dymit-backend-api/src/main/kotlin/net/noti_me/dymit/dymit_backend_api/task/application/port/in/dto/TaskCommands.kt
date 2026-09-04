package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import java.time.Instant

/**
 * 과제 생성 커맨드입니다.
 */
data class CreateTaskCommand(
    val relatedScheduleId: String,
    val title: String,
    val description: String,
    val attachmentFileIds: List<String>,
    val expireAt: Instant,
    val assigneeMemberIds: List<String> = emptyList(),
    val submissionType: TaskSubmissionType = TaskSubmissionType.OUTPUT
)

/**
 * 과제 수정 커맨드입니다.
 */
data class UpdateTaskCommand(
    val title: String,
    val description: String,
    val attachmentFileIds: List<String>,
    val expireAt: Instant,
    val assigneeMemberIds: List<String>? = null
)

/**
 * 과제 제출 생성 커맨드입니다.
 */
data class CreateTaskSubmissionCommand(
    val title: String,
    val content: String,
    val attachments: List<TaskSubmissionAttachmentCommand>
)

/**
 * 과제 제출 수정 커맨드입니다.
 */
data class UpdateTaskSubmissionCommand(
    val title: String,
    val content: String,
    val attachments: List<TaskSubmissionAttachmentCommand>
)

/**
 * 과제 제출 첨부 커맨드입니다.
 */
data class TaskSubmissionAttachmentCommand(
    val type: TaskSubmitAttachmentType,
    val title: String,
    val url: String? = null,
    val fileId: String? = null
)

/**
 * 과제 댓글 생성 커맨드입니다.
 */
data class CreateTaskSubmissionCommentCommand(
    val content: String
)

/**
 * 과제 댓글 수정 커맨드입니다.
 */
data class UpdateTaskSubmissionCommentCommand(
    val content: String
)

/** 과제 생성 유즈케이스 입력입니다. */
data class CreateTaskInput(
    val memberInfo: MemberInfo,
    val groupId: String,
    val command: CreateTaskCommand
)

/** 과제 수정 유즈케이스 입력입니다. */
data class UpdateTaskInput(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val command: UpdateTaskCommand
)

/** 과제 삭제 명령입니다. */
data class RemoveTaskCommand(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String
)

/** 그룹 과제 목록 조회 입력입니다. */
data class GetGroupTasksQuery(
    val memberInfo: MemberInfo,
    val groupId: String
)

/** 과제 상세 조회 입력입니다. */
data class GetTaskDetailQuery(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String
)

/** 과제 제출 생성 유즈케이스 입력입니다. */
data class CreateSubmissionInput(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val command: CreateTaskSubmissionCommand
)

/** 과제 제출 수정 유즈케이스 입력입니다. */
data class UpdateSubmissionInput(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val submissionId: String,
    val command: UpdateTaskSubmissionCommand
)

/** 과제 제출 철회 명령입니다. */
data class WithdrawSubmissionCommand(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val submissionId: String
)

/** 체크형 과제 제출 철회 명령입니다. */
data class WithdrawCheckSubmissionByAssigneeCommand(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val assigneeId: String
)

/** 과제 제출 목록 조회 입력입니다. */
data class GetTaskSubmissionsQuery(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String
)

/** 과제 제출 단건 조회 입력입니다. */
data class GetTaskSubmissionQuery(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val memberId: String
)

/** 과제 제출 댓글 생성 유즈케이스 입력입니다. */
data class CreateSubmissionCommentInput(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val submissionId: String,
    val command: CreateTaskSubmissionCommentCommand
)

/** 과제 제출 댓글 수정 유즈케이스 입력입니다. */
data class UpdateSubmissionCommentInput(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val submissionId: String,
    val commentId: String,
    val command: UpdateTaskSubmissionCommentCommand
)

/** 과제 제출 댓글 삭제 명령입니다. */
data class DeleteSubmissionCommentCommand(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val submissionId: String,
    val commentId: String
)

/** 과제 제출 댓글 목록 조회 입력입니다. */
data class GetSubmissionCommentsQuery(
    val memberInfo: MemberInfo,
    val groupId: String,
    val taskId: String,
    val submissionId: String
)

/** 과제 대상자 목록 조회 입력입니다. */
data class GetTaskAssigneesQuery(
    val memberInfo: MemberInfo,
    val taskId: String
)

/** 사전 과제 대상자 추가 명령입니다. */
data class AddAssigneeToPreTasksCommand(
    val scheduleId: String,
    val memberId: String
)

/** 일정 참여 사전 과제 동기화 명령입니다. */
data class SyncParticipatedScheduleTasksCommand(
    val scheduleId: String,
    val memberId: String
)

/** 사전 과제 대상자 제거 명령입니다. */
data class RemoveAssigneeFromPreTasksCommand(
    val scheduleId: String,
    val memberId: String
)

/** 취소 일정 연관 과제 삭제 명령입니다. */
data class RemoveTasksByCanceledScheduleCommand(
    val scheduleId: String,
    val groupId: String
)
