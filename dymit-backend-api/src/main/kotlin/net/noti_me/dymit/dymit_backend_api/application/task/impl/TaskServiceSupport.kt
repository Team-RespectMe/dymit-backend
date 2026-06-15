package net.noti_me.dymit.dymit_backend_api.application.task.impl

import net.noti_me.dymit.dymit_backend_api.application.file.FileServiceFacade
import net.noti_me.dymit.dymit_backend_api.application.file.FileUrlResolver
import net.noti_me.dymit.dymit_backend_api.application.file.dto.UpdateFileStatusCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeSummaryDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAssigneeMemberDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskAttachmentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionAttachmentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionAttachmentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFile
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionComment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmitAttachmentType
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.ports.persistence.file.UserFileRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskAssigneeRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionCommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 과제 서비스 내부 공통 로직 지원 컴포넌트입니다.
 */
@Component
class TaskServiceSupport(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val groupMemberRepository: StudyGroupMemberRepository,
    private val studyScheduleRepository: StudyScheduleRepository,
    private val scheduleParticipantRepository: ScheduleParticipantRepository,
    private val taskRepository: TaskRepository,
    private val taskAssigneeRepository: TaskAssigneeRepository,
    private val taskSubmissionRepository: TaskSubmissionRepository,
    private val taskSubmissionCommentRepository: TaskSubmissionCommentRepository,
    private val userFileRepository: UserFileRepository,
    private val fileServiceFacade: FileServiceFacade,
    private val fileUrlResolver: FileUrlResolver
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
        return studyScheduleRepository.loadById(toObjectId(scheduleId, "relatedScheduleId"))
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

    fun loadTasksBySchedule(scheduleId: ObjectId, type: TaskType): List<Task> {
        return taskRepository.findByRelatedScheduleIdAndType(scheduleId, type)
    }

    fun loadTasksBySchedule(scheduleId: ObjectId): List<Task> {
        return taskRepository.findByRelatedScheduleId(scheduleId)
    }

    fun loadTasksByGroup(groupId: ObjectId): List<Task> {
        val schedules = studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(groupId)
        val scheduleIds = schedules.mapNotNull { it.id }
        return taskRepository.findByRelatedScheduleIds(scheduleIds)
    }

    fun loadSubmissionsByTask(taskId: ObjectId): List<TaskSubmission> {
        return taskSubmissionRepository.findByTaskId(taskId)
    }

    fun loadAssigneeMemberIdsByTask(taskId: ObjectId): List<ObjectId> {
        return taskAssigneeRepository.findByTaskId(taskId).map { it.memberId }
    }

    fun loadSubmissionByTaskAndMember(taskId: ObjectId, memberId: ObjectId): TaskSubmission {
        return taskSubmissionRepository.findByTaskIdAndMemberId(taskId, memberId)
            ?: throw NotFoundException(message = "존재하지 않는 제출입니다.")
    }

    fun loadCommentsBySubmission(submissionId: ObjectId): List<TaskSubmissionComment> {
        return taskSubmissionCommentRepository.findBySubmissionId(submissionId)
    }

    fun loadScheduleParticipantIds(scheduleId: ObjectId): List<ObjectId> {
        return scheduleParticipantRepository.getByScheduleId(scheduleId).map { it.memberId }
    }

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
        val schedule = studyScheduleRepository.loadById(task.relatedScheduleId)
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
        val participants = scheduleParticipantRepository.getByScheduleId(scheduleId)
        val assignees = participants.map { TaskAssignee(taskId = taskId, memberId = it.memberId) }
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

    fun removeCommentsByTask(taskId: ObjectId) {
        taskSubmissionCommentRepository.deleteByTaskId(taskId)
    }

    fun removeCommentsBySubmission(submissionId: ObjectId) {
        taskSubmissionCommentRepository.deleteBySubmissionId(submissionId)
    }

    fun removeSubmissionsByTask(taskId: ObjectId) {
        taskSubmissionRepository.deleteByTaskId(taskId)
    }

    fun removeSubmissionById(submissionId: ObjectId) {
        taskSubmissionRepository.deleteById(submissionId)
    }

    fun removeTask(taskId: ObjectId) {
        taskRepository.deleteById(taskId)
    }

    fun saveTask(task: Task): Task {
        return taskRepository.save(task)
    }

    fun saveAssignee(assignee: TaskAssignee): TaskAssignee {
        return taskAssigneeRepository.save(assignee)
    }

    fun saveSubmission(submission: TaskSubmission): TaskSubmission {
        return taskSubmissionRepository.save(submission)
    }

    fun saveComment(comment: TaskSubmissionComment): TaskSubmissionComment {
        return taskSubmissionCommentRepository.save(comment)
    }

    fun deleteComment(commentId: ObjectId) {
        taskSubmissionCommentRepository.deleteById(commentId)
    }

    fun validateTaskAttachmentFiles(fileIds: List<ObjectId>) {
        val files = loadFiles(fileIds)
        files.forEach { file ->
            if ( file.status != UserFileStatus.UPLOADED && file.status != UserFileStatus.LINKED && file.status != UserFileStatus.UNREFERENCED ) {
                throw BadRequestException(message = "업로드된 파일만 첨부할 수 있습니다.")
            }
        }
    }

    fun validateSubmissionAttachmentFiles(fileIds: List<ObjectId>) {
        val files = loadFiles(fileIds)
        files.forEach { file ->
            if ( file.status != UserFileStatus.UPLOADED && file.status != UserFileStatus.LINKED && file.status != UserFileStatus.UNREFERENCED ) {
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

    fun updateFileStatuses(fileIds: Collection<ObjectId>, status: UserFileStatus) {
        fileIds.distinct().forEach { fileId ->
            fileServiceFacade.updateFileStatus(
                UpdateFileStatusCommand(
                    fileId = fileId.toHexString(),
                    status = status
                )
            )
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
        updateFileStatuses(orphaned, UserFileStatus.UNREFERENCED)
    }

    fun toTaskDto(task: Task, groupId: ObjectId): TaskDto {
        val files = userFileRepository.findByIds(task.attachments.map { it.fileId }).associateBy { it.id!! }
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
                    url = fileUrlResolver.resolve(file.path),
                    thumbnailUrl = fileUrlResolver.resolveOrNull(file.thumbnailPath),
                    status = file.status
                )
            },
            expireAt = task.expireAt,
            submittedAssigneeCount = assignees.count { it.status == TaskAssigneeStatus.SUBMITTED },
            notSubmittedAssigneeCount = assignees.count { it.status == TaskAssigneeStatus.NOT_SUBMITTED },
            assignees = assignees.map { assignee ->
                val member = members[assignee.memberId] ?: throw NotFoundException(message = "그룹 멤버 정보를 찾을 수 없습니다.")
                TaskAssigneeSummaryDto(
                    memberId = member.memberId.toHexString(),
                    nickname = member.nickname,
                    profileImageUrl = member.profileImage.url,
                    profileImageType = member.profileImage.type,
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
                    profileImage = member.profileImage
                )
            )
        }
    }

    fun toSubmissionDto(submission: TaskSubmission, groupId: ObjectId): TaskSubmissionDto {
        val member = groupMemberRepository.findByGroupIdAndMemberId(groupId, submission.memberId)
            ?: throw NotFoundException(message = "그룹 멤버 정보를 찾을 수 없습니다.")

        val fileIds = submissionAttachmentFileIds(submission.attachments)
        val files = userFileRepository.findByIds(fileIds).associateBy { it.id!! }

        return TaskSubmissionDto(
            submissionId = submission.identifier,
            taskId = submission.taskId.toHexString(),
            memberId = submission.memberId.toHexString(),
            memberNickname = member.nickname,
            memberProfileImageUrl = member.profileImage.url,
            memberProfileImageType = member.profileImage.type,
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
                        fileUrl = fileUrlResolver.resolve(file.path),
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
            writerProfileImageType = member.profileImage.type,
            content = comment.content,
            createdAt = comment.createdAt
        )
    }

    private fun loadFiles(fileIds: List<ObjectId>): List<UserFile> {
        if ( fileIds.isEmpty() ) {
            return emptyList()
        }

        val files = userFileRepository.findByIds(fileIds)
        if ( files.size != fileIds.size ) {
            throw NotFoundException(message = "존재하지 않는 파일이 포함되어 있습니다.")
        }
        return files
    }
}
