package net.noti_me.dymit.dymit_backend_api.application.task.dto

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupProfileImageDto as ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import java.time.LocalDateTime

/**
 * 과제 조회 DTO입니다.
 */
data class TaskDto(
    val taskId: String,
    val relatedScheduleId: String,
    val type: TaskType,
    val title: String,
    val description: String,
    val attachments: List<TaskAttachmentDto>,
    val expireAt: LocalDateTime,
    val submittedAssigneeCount: Int,
    val notSubmittedAssigneeCount: Int,
    val assignees: List<TaskAssigneeSummaryDto>,
    val submissionType: TaskSubmissionType = TaskSubmissionType.OUTPUT
)

/**
 * 과제 첨부 조회 DTO입니다.
 */
data class TaskAttachmentDto(
    val fileId: String,
    val originalFileName: String,
    val url: String,
    val thumbnailUrl: String?,
    val status: TaskFileStatusDto
)

/**
 * 과제 대상자 요약 DTO입니다.
 */
data class TaskAssigneeSummaryDto(
    val memberId: String,
    val nickname: String,
    val profileImageUrl: String,
    val profileImageType: ProfileImageType,
    val status: TaskAssigneeStatus
)

/**
 * 과제 제출 조회 DTO입니다.
 */
data class TaskSubmissionDto(
    val submissionId: String,
    val taskId: String,
    val memberId: String,
    val memberNickname: String,
    val memberProfileImageUrl: String,
    val memberProfileImageType: ProfileImageType,
    val title: String,
    val content: String,
    val attachments: List<TaskSubmissionAttachmentDto>,
    val createdAt: LocalDateTime?
)

/**
 * 과제 제출 첨부 조회 DTO입니다.
 */
data class TaskSubmissionAttachmentDto(
    val type: TaskSubmitAttachmentType,
    val title: String,
    val url: String?,
    val fileId: String?,
    val fileUrl: String?,
    val originalFileName: String?
)

/**
 * 과제 제출 댓글 조회 DTO입니다.
 */
data class TaskSubmissionCommentDto(
    val commentId: String,
    val taskId: String,
    val submissionId: String,
    val writerId: String,
    val writerNickname: String,
    val writerProfileImageUrl: String,
    val writerProfileImageType: ProfileImageType,
    val content: String,
    val createdAt: LocalDateTime?
)

/**
 * 과제 제출 대상 조회 DTO입니다.
 */
data class TaskAssigneeDto(
    val groupId: String = "",
    val taskId: String,
    val member: TaskAssigneeMemberDto
)

/**
 * 과제 제출 대상 회원 조회 DTO입니다.
 */
data class TaskAssigneeMemberDto(
    val id: String,
    val nickname: String,
    val profileImage: ProfileImageVo
)
