package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeSummaryDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAssigneeMemberDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskAttachmentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionAttachmentCommand
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionAttachmentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskProfileImageDto
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.StudyScheduleQueryPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleServerDto as StudySchedule
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionComment
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmitAttachment
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupQueryPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskAssigneeRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskSubmissionRepository
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.TaskFilePort
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 과제 서비스 내부 공통 로직 지원 컴포넌트입니다.
 */
@Component
class TaskServiceSupport(
    private val loadStudyGroupPort: StudyGroupQueryPort,
    private val groupMemberRepository: StudyGroupMemberPort,
    private val studyScheduleQueryPort: StudyScheduleQueryPort,
    private val taskRepository: TaskRepository,
    private val taskAssigneeRepository: TaskAssigneeRepository,
    private val taskSubmissionRepository: TaskSubmissionRepository,
    private val taskSubmissionCommentRepository: TaskSubmissionCommentRepository,
    private val taskFilePort: TaskFilePort
) {
    fun toObjectId(value: String, fieldName: String): ObjectId {
        if ( !ObjectId.isValid(value) ) {
            throw BadRequestException(message = "${fieldName} 형식이 올바르지 않습니다.")
        }
        return ObjectId(value)
    }

    fun toObjectIds(values: List<String>, fieldName: String): List<ObjectId> {
        return values.mapIndexed { index, value ->
            toObjectId(value, "${fieldName}[${index}]")
        }
    }

    fun loadGroup(groupId: String): StudyGroup {
        return loadStudyGroupPort.loadByGroupId(groupId)
            ?: throw NotFoundException(message = "존재하지 않는 스터디 그룹입니다.")
    }

    fun loadSchedule(scheduleId: String): StudySchedule {
        return studyScheduleQueryPort.loadById(toObjectId(scheduleId, "relatedScheduleId"))
            ?: throw NotFoundException(message = "존재하지 않는 일정입니다.")
    }

    fun loadTask(taskId: String): Task {
        return taskRepository.findById(toObjectId(taskId, "taskId"))
            ?: throw NotFoundException(message = "존재하지 않는 과제입니다.")
    }

    fun loadSubmission(submissionId: String): TaskSubmission {
        return taskSubmissionRepository.findById(toObjectId(submissionId, "submissionId"))
            ?: throw NotFoundException(message = "존재하지 않는 제출입니다.")
    }

    fun loadComment(commentId: String): TaskSubmissionComment {
        return taskSubmissionCommentRepository.findById(toObjectId(commentId, "commentId"))
            ?: throw NotFoundException(message = "존재하지 않는 댓글입니다.")
    }

    fun loadTasksBySchedule(
        scheduleId: ObjectId,
        type: TaskType
    ): List<Task> = taskRepository.findByRelatedScheduleIdAndType(scheduleId, type)

    fun loadTasksBySchedule(scheduleId: ObjectId): List<Task> =
        taskRepository.findByRelatedScheduleId(scheduleId)

    fun loadTasksByGroup(groupId: ObjectId): List<Task> {
        val schedules = studyScheduleQueryPort.loadByGroupIdOrderByScheduleAtDesc(groupId)
        val scheduleIds = schedules.map { it.id }
        return taskRepository.findByRelatedScheduleIds(scheduleIds)
    }

    fun loadSubmissionsByTask(taskId: ObjectId): List<TaskSubmission> =
        taskSubmissionRepository.findByTaskId(taskId)

    fun loadAssigneeMemberIdsByTask(taskId: ObjectId): List<ObjectId> =
        taskAssigneeRepository.findByTaskId(taskId).map { it.memberId }

    fun loadSubmissionByTaskAndMember(taskId: ObjectId, memberId: ObjectId): TaskSubmission {
        return taskSubmissionRepository.findByTaskIdAndMemberId(taskId, memberId)
            ?: throw NotFoundException(message = "존재하지 않는 제출입니다.")
    }

    fun loadCommentsBySubmission(submissionId: ObjectId): List<TaskSubmissionComment> =
        taskSubmissionCommentRepository.findBySubmissionId(submissionId)

    fun loadScheduleParticipantIds(scheduleId: ObjectId): List<ObjectId> =
        studyScheduleQueryPort.getParticipantMemberIds(scheduleId)

    fun checkOwner(memberInfo: MemberInfo, group: StudyGroup) {
        if ( group.ownerId.toHexString() != memberInfo.memberId ) {
            throw ForbiddenException(message = "스터디 그룹 소유자만 요청할 수 있습니다.")
        }
    }

    fun checkScheduleInGroup(schedule: StudySchedule, groupId: ObjectId) {
        if ( schedule.groupId != groupId ) {
            throw ForbiddenException(message = "해당 그룹의 일정이 아닙니다.")
        }
    }

    fun checkTaskInGroup(task: Task, groupId: ObjectId) {
        val schedule = studyScheduleQueryPort.loadById(task.relatedScheduleId)
            ?: throw NotFoundException(message = "연관 일정을 찾을 수 없습니다.")

        if ( schedule.groupId != groupId ) {
            throw ForbiddenException(message = "해당 그룹의 과제가 아닙니다.")
        }
    }

    fun checkTaskActionAllowedBySchedule(task: Task) {
        if ( TaskExpireAtNormalizer.isExpired(task.expireAt) ) {
            throw BadRequestException(message = "마감된 과제는 수정/삭제할 수 없습니다.")
        }
    }

    fun requireGroupMember(groupId: ObjectId, memberId: ObjectId): StudyGroupMember {
        return groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
            ?: throw ForbiddenException(message = "해당 그룹의 멤버가 아닙니다.")
    }

    fun requireTaskAssignee(taskId: ObjectId, memberId: ObjectId): TaskAssignee {
        return taskAssigneeRepository.findByTaskIdAndMemberId(taskId, memberId)
            ?: throw ForbiddenException(message = "과제 대상자만 제출/댓글을 변경할 수 있습니다.")
    }

    fun resolveTaskTypeBySchedule(schedule: StudySchedule, requestedAt: LocalDateTime): TaskType {
        return if ( TaskExpireAtNormalizer.toKst(schedule.scheduleAt).isAfter(TaskExpireAtNormalizer.toKst(requestedAt)) ) {
            TaskType.PRE
        } else {
            TaskType.POST
        }
    }

    fun resolveTaskTypeBySchedule(schedule: StudySchedule): TaskType {
        return resolveTaskTypeBySchedule(schedule, TaskExpireAtNormalizer.currentUtcDateTime())
    }

    fun validatePreTaskCreatable(schedule: StudySchedule, requestedAt: LocalDateTime) {
        if ( TaskExpireAtNormalizer.toKst(requestedAt).isAfter(TaskExpireAtNormalizer.toKst(schedule.scheduleAt).minusHours(24)) ) {
            throw BadRequestException(message = "사전 과제는 일정 시작 24시간 이전에만 생성할 수 있습니다.")
        }
    }

    fun normalizeExpireAtForCreate(
        type: TaskType,
        requestedExpireAt: LocalDateTime,
        schedule: StudySchedule
    ): LocalDateTime {
        return if ( type == TaskType.PRE ) {
            schedule.scheduleAt
        } else {
            TaskExpireAtNormalizer.normalizePostExpireAt(requestedExpireAt)
        }
    }

    fun normalizeExpireAtForUpdate(
        type: TaskType,
        requestedExpireAt: LocalDateTime,
        currentExpireAt: LocalDateTime
    ): LocalDateTime {
        return if ( type == TaskType.PRE ) {
            currentExpireAt
        } else {
            TaskExpireAtNormalizer.normalizePostExpireAt(requestedExpireAt)
        }
    }

    fun checkSubmissionUpdatable(task: Task) {
        if ( TaskExpireAtNormalizer.isExpired(task.expireAt) ) {
            throw BadRequestException(message = "마감된 과제는 제출/수정/철회할 수 없습니다.")
        }
    }

    fun initializeAssigneesForPreTask(taskId: ObjectId, scheduleId: ObjectId) {
        val participantIds = studyScheduleQueryPort.getParticipantMemberIds(scheduleId)
        val assignees = participantIds.map { TaskAssignee(taskId = taskId, memberId = it) }
        taskAssigneeRepository.saveAll(assignees)
    }

    fun initializeAssignees(taskId: ObjectId, memberIds: List<ObjectId>) {
        val assignees = memberIds.distinct().map { memberId ->
            TaskAssignee(taskId = taskId, memberId = memberId)
        }
        taskAssigneeRepository.saveAll(assignees)
    }

    fun validateAssigneeMembersInGroup(groupId: ObjectId, memberIds: List<ObjectId>) {
        if ( memberIds.isEmpty() ) {
            return
        }

        val members = groupMemberRepository.findByGroupIdAndMemberIdsIn(groupId, memberIds.distinct())
        if ( members.size != memberIds.distinct().size ) {
            throw BadRequestException(message = "과제 대상자는 모두 그룹 멤버여야 합니다.")
        }
    }

    fun addAssigneeIfAbsent(taskId: ObjectId, memberId: ObjectId): Boolean {
        if ( taskAssigneeRepository.existsByTaskIdAndMemberId(taskId, memberId) ) {
            return false
        }
        taskAssigneeRepository.save(TaskAssignee(taskId = taskId, memberId = memberId))
        return true
    }

    fun removeAssignee(taskId: ObjectId, memberId: ObjectId) {
        taskAssigneeRepository.deleteByTaskIdAndMemberId(taskId, memberId)
    }

    fun removeAssigneesByTask(taskId: ObjectId) {
        taskAssigneeRepository.deleteByTaskId(taskId)
    }

    /**
     * 과제 대상자를 제거하면서 제출, 댓글, 고아 첨부 파일을 함께 정리합니다.
     *
     * @param taskId 과제 ID
     * @param memberId 제거할 멤버 ID
     */
    fun removeAssigneeWithSubmissionCleanup(taskId: ObjectId, memberId: ObjectId) {
        val removedSubmission = removeSubmissionAndCommentsByTaskAndMember(taskId, memberId)
        if ( removedSubmission != null ) {
            val fileIds = submissionAttachmentFileIds(removedSubmission.attachments)
            downgradeOrphanedFiles(fileIds)
        }
        removeAssignee(taskId, memberId)
    }

    fun removeSubmissionAndCommentsByTaskAndMember(taskId: ObjectId, memberId: ObjectId): TaskSubmission? {
        val deleted = taskSubmissionRepository.deleteByTaskIdAndMemberId(taskId, memberId)
        if ( deleted != null ) {
            taskSubmissionCommentRepository.deleteBySubmissionId(deleted.id!!)
        }
        return deleted
    }

    fun removeCommentsByTask(taskId: ObjectId) =
        taskSubmissionCommentRepository.deleteByTaskId(taskId)

    fun removeCommentsBySubmission(submissionId: ObjectId) =
        taskSubmissionCommentRepository.deleteBySubmissionId(submissionId)

    fun removeSubmissionsByTask(taskId: ObjectId) =
        taskSubmissionRepository.deleteByTaskId(taskId)

    fun removeSubmissionById(submissionId: ObjectId) =
        taskSubmissionRepository.deleteById(submissionId)

    fun removeTask(taskId: ObjectId) = taskRepository.deleteById(taskId)

    fun saveTask(task: Task): Task = taskRepository.save(task)

    fun saveAssignee(assignee: TaskAssignee): TaskAssignee =
        taskAssigneeRepository.save(assignee)

    fun saveSubmission(submission: TaskSubmission): TaskSubmission =
        taskSubmissionRepository.save(submission)

    fun saveComment(comment: TaskSubmissionComment): TaskSubmissionComment {
        return taskSubmissionCommentRepository.save(comment)
    }

    fun deleteComment(commentId: ObjectId) {
        taskSubmissionCommentRepository.deleteById(commentId)
    }

    fun validateTaskAttachmentFiles(fileIds: List<ObjectId>) {
        val files = loadFiles(fileIds)
        files.forEach { file ->
            if ( file.status != TaskFileStatusDto.UPLOADED &&
                file.status != TaskFileStatusDto.LINKED &&
                file.status != TaskFileStatusDto.UNREFERENCED
            ) {
                throw BadRequestException(message = "업로드된 파일만 첨부할 수 있습니다.")
            }
        }
    }

    fun validateSubmissionAttachmentFiles(fileIds: List<ObjectId>) {
        val files = loadFiles(fileIds)
        files.forEach { file ->
            if ( file.status != TaskFileStatusDto.UPLOADED &&
                file.status != TaskFileStatusDto.LINKED &&
                file.status != TaskFileStatusDto.UNREFERENCED
            ) {
                throw BadRequestException(message = "업로드된 파일만 첨부할 수 있습니다.")
            }
        }
    }

    fun toSubmissionAttachments(attachments: List<TaskSubmissionAttachmentCommand>): List<TaskSubmitAttachment> {
        return attachments.map { attachment ->
            TaskSubmitAttachment(
                type = attachment.type,
                title = attachment.title,
                url = attachment.url,
                fileId = attachment.fileId?.let { toObjectId(it, "attachments.fileId") }
            )
        }
    }

    fun submissionAttachmentFileIds(attachments: List<TaskSubmitAttachment>): List<ObjectId> {
        return attachments.filter { it.type == TaskSubmitAttachmentType.FILE && it.fileId != null }
            .map { it.fileId!! }
    }

    fun updateFileStatuses(fileIds: Collection<ObjectId>, status: TaskFileStatusDto) {
        fileIds.distinct().forEach { fileId ->
            taskFilePort.updateStatus(fileId, status)
                ?: throw NotFoundException(message = "존재하지 않는 파일입니다.")
        }
    }

    fun downgradeOrphanedFiles(fileIds: Collection<ObjectId>) {
        val targets = fileIds.distinct()
        if ( targets.isEmpty() ) {
            return
        }

        val taskReferences = taskRepository.findAttachedFileIds(targets)
        val submissionReferences = taskSubmissionRepository.findAttachedFileIds(targets)
        val referenced = taskReferences + submissionReferences
        val orphaned = targets.filter { !referenced.contains(it) }
        updateFileStatuses(orphaned, TaskFileStatusDto.UNREFERENCED)
    }
    fun toTaskDto(
        task: Task,
        groupId: ObjectId,
        allowMissingAssignee: Boolean = false
    ): TaskDto {
        val files = taskFilePort.loadByIds(task.attachments.map { it.fileId }).associateBy { it.id }
        val assignees = taskAssigneeRepository.findByTaskId(task.id!!)
        val members = groupMemberRepository.findByGroupIdAndMemberIdsIn(groupId, assignees.map { it.memberId })
            .associateBy { it.memberId }
        return TaskDto(
            taskId = task.identifier,
            relatedScheduleId = task.relatedScheduleId.toHexString(),
            type = task.type,
            title = task.title,
            description = task.description,
            submissionType = task.submissionType,
            attachments = task.attachments.map { attachment ->
                val file = files[attachment.fileId] ?: throw NotFoundException(message = "존재하지 않는 파일입니다.")
                TaskAttachmentDto(
                    fileId = file.identifier,
                    originalFileName = file.originalFileName,
                    url = file.url,
                    thumbnailUrl = file.thumbnailUrl,
                    status = file.status
                )
            },
            expireAt = task.expireAt,
            submittedAssigneeCount = assignees.count { it.status == TaskAssigneeStatus.SUBMITTED },
            notSubmittedAssigneeCount = assignees.count { it.status == TaskAssigneeStatus.NOT_SUBMITTED },
            assignees = assignees.map { assignee ->
                val member = members[assignee.memberId]
                    ?: if (allowMissingAssignee) null else throw NotFoundException(message = "그룹 멤버 정보를 찾을 수 없습니다.")
                TaskAssigneeSummaryDto(
                    memberId = assignee.memberId.toHexString(),
                    nickname = member?.nickname ?: "탈퇴한 회원",
                    profileImageUrl = member?.profileImage?.url ?: "https://d380gc0prbxdbr.cloudfront.net/static/presets/members/kick_64x64.png",
                    profileImageType = member?.profileImage?.type?.name?.let(TaskProfileImageType::valueOf) ?: TaskProfileImageType.PRESET,
                    status = assignee.status
                )
            }
        )
    }

    fun toTaskAssigneeDtos(taskId: ObjectId, groupId: ObjectId): List<TaskAssigneeDto> {
        val assignees = taskAssigneeRepository.findByTaskId(taskId)
        val members = groupMemberRepository.findByGroupIdAndMemberIdsIn(groupId, assignees.map { it.memberId })
            .associateBy { it.memberId }

        return assignees.map { assignee ->
            val member = members[assignee.memberId]
                ?: throw NotFoundException(message = "그룹 멤버 정보를 찾을 수 없습니다.")

            TaskAssigneeDto(
                groupId = groupId.toHexString(),
                taskId = assignee.taskId.toHexString(),
                member = TaskAssigneeMemberDto(
                    id = member.memberId.toHexString(),
                    nickname = member.nickname,
                    profileImage = TaskProfileImageDto(
                        type = TaskProfileImageType.valueOf(member.profileImage.type.name),
                        url = member.profileImage.url
                    )
                )
            )
        }
    }

    fun toSubmissionDto(submission: TaskSubmission, groupId: ObjectId): TaskSubmissionDto {
        val member = groupMemberRepository.findByGroupIdAndMemberId(groupId, submission.memberId)
            ?: throw NotFoundException(message = "그룹 멤버 정보를 찾을 수 없습니다.")

        val fileIds = submissionAttachmentFileIds(submission.attachments)
        val files = taskFilePort.loadByIds(fileIds).associateBy { it.id }

        return TaskSubmissionDto(
            submissionId = submission.identifier,
            taskId = submission.taskId.toHexString(),
            memberId = submission.memberId.toHexString(),
            memberNickname = member.nickname,
            memberProfileImageUrl = member.profileImage.url,
            memberProfileImageType = TaskProfileImageType.valueOf(member.profileImage.type.name),
            title = submission.title,
            content = submission.content,
            attachments = submission.attachments.map { attachment ->
                if ( attachment.type == TaskSubmitAttachmentType.URL ) {
                    TaskSubmissionAttachmentDto(
                        type = attachment.type,
                        title = attachment.title,
                        url = attachment.url,
                        fileId = null,
                        fileUrl = null,
                        originalFileName = null
                    )
                } else {
                    val file = files[attachment.fileId] ?: throw NotFoundException(message = "존재하지 않는 파일입니다.")
                    TaskSubmissionAttachmentDto(
                        type = attachment.type,
                        title = attachment.title,
                        url = null,
                        fileId = file.identifier,
                        fileUrl = file.url,
                        originalFileName = file.originalFileName
                    )
                }
            },
            createdAt = submission.createdAt
        )
    }

    fun toCommentDto(comment: TaskSubmissionComment, groupId: ObjectId): TaskSubmissionCommentDto {
        val member = groupMemberRepository.findByGroupIdAndMemberId(groupId, comment.writerId)
            ?: throw NotFoundException(message = "그룹 멤버 정보를 찾을 수 없습니다.")

        return TaskSubmissionCommentDto(
            commentId = comment.identifier,
            taskId = comment.taskId.toHexString(),
            submissionId = comment.submissionId.toHexString(),
            writerId = member.memberId.toHexString(),
            writerNickname = member.nickname,
            writerProfileImageUrl = member.profileImage.url,
            writerProfileImageType = TaskProfileImageType.valueOf(member.profileImage.type.name),
            content = comment.content,
            createdAt = comment.createdAt
        )
    }

    private fun loadFiles(fileIds: List<ObjectId>): List<TaskFileDto> {
        if ( fileIds.isEmpty() ) {
            return emptyList()
        }

        val files = taskFilePort.loadByIds(fileIds)
        if ( files.size != fileIds.size ) {
            throw NotFoundException(message = "존재하지 않는 파일이 포함되어 있습니다.")
        }
        return files
    }
}
