package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.UpdateTaskUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAttachment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskModifiedEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 과제 수정 유즈케이스 구현체입니다.
 */
@Service
class UpdateTaskUseCaseImpl(
    private val support: TaskServiceSupport,
    private val eventPublisher: ApplicationEventPublisher
) : UpdateTaskUseCase {

    override fun updateTask(memberInfo: MemberInfo, groupId: String, taskId: String, command: UpdateTaskCommand): TaskDto {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val group = support.loadGroup(groupId)
        support.checkOwner(memberInfo, group)

        val task = support.loadTask(taskId)
        val taskObjectId = requireNotNull(task.id)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkTaskActionAllowedBySchedule(task)
        val expireAt = support.normalizeExpireAtForUpdate(task.type, command.expireAt, task.expireAt)
        val requestedAssigneeMemberIds = resolveRequestedAssigneeMemberIds(task.type, command)
        if ( requestedAssigneeMemberIds != null ) {
            support.validateAssigneeMembersInGroup(groupIdObjectId, requestedAssigneeMemberIds)
        }

        val oldAttachmentIds = task.attachments.map { it.fileId }.toSet()
        val newAttachmentIds = support.toObjectIds(command.attachmentFileIds.distinct(), "attachmentFileIds")
        support.validateTaskAttachmentFiles(newAttachmentIds)

        task.update(
            title = command.title,
            description = command.description,
            attachments = newAttachmentIds.map { TaskAttachment(fileId = it) },
            expireAt = expireAt
        )
        val saved = support.saveTask(task)
        if ( requestedAssigneeMemberIds != null ) {
            syncPostTaskAssignees(taskObjectId, requestedAssigneeMemberIds)
        }

        val newAttachmentSet = newAttachmentIds.toSet()
        val linked = newAttachmentSet.filter { !oldAttachmentIds.contains(it) }
        val removed = oldAttachmentIds.filter { !newAttachmentSet.contains(it) }
        support.updateFileStatuses(linked, UserFileStatus.LINKED)
        support.downgradeOrphanedFiles(removed)

        eventPublisher.publishEvent(
            TaskModifiedEvent(
                taskId = saved.id!!,
                groupId = groupIdObjectId,
                scheduleId = saved.relatedScheduleId,
                task = saved,
                group = group
            )
        )
        return support.toTaskDto(saved, groupIdObjectId)
    }

    /**
     * 수정 요청에서 대상자 변경 목록을 해석합니다.
     *
     * @param taskType 과제 타입
     * @param command 과제 수정 커맨드
     * @return POST 과제의 대상자 변경 목록, 변경이 없으면 null
     */
    private fun resolveRequestedAssigneeMemberIds(
        taskType: TaskType,
        command: UpdateTaskCommand
    ): List<ObjectId>? {
        if ( taskType != TaskType.POST || command.assigneeMemberIds == null ) {
            return null
        }

        return support.toObjectIds(command.assigneeMemberIds.distinct(), "assigneeMemberIds")
    }

    /**
     * 사후 과제 대상자를 요청 목록과 동기화합니다.
     *
     * @param taskId 과제 ID
     * @param requestedAssigneeMemberIds 요청된 대상자 멤버 ID 목록
     */
    private fun syncPostTaskAssignees(taskId: ObjectId, requestedAssigneeMemberIds: List<ObjectId>) {
        val currentAssigneeMemberIds = support.loadAssigneeMemberIdsByTask(taskId).distinct()
        val currentAssigneeMemberIdSet = currentAssigneeMemberIds.toSet()
        val requestedAssigneeMemberIdSet = requestedAssigneeMemberIds.toSet()

        currentAssigneeMemberIds
            .filter { !requestedAssigneeMemberIdSet.contains(it) }
            .forEach { memberId ->
                removePostTaskAssignee(taskId, memberId)
            }

        requestedAssigneeMemberIds
            .filter { !currentAssigneeMemberIdSet.contains(it) }
            .forEach { memberId ->
                support.addAssigneeIfAbsent(taskId, memberId)
            }
    }

    /**
     * 사후 과제 대상자 제거 시 연관 제출 데이터를 정리합니다.
     *
     * @param taskId 과제 ID
     * @param memberId 제거할 멤버 ID
     */
    private fun removePostTaskAssignee(taskId: ObjectId, memberId: ObjectId) {
        support.removeAssigneeWithSubmissionCleanup(taskId, memberId)
    }
}
