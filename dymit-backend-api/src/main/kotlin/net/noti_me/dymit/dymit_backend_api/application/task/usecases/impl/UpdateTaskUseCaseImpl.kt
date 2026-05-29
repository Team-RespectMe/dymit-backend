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
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkTaskActionAllowedBySchedule(task)
        val expireAt = support.normalizeExpireAtForUpdate(task.type, command.expireAt, task.expireAt)

        val oldAttachmentIds = task.attachments.map { it.fileId }.toSet()
        val newAttachmentIds = support.toObjectIds(command.attachmentFileIds.distinct(), "attachmentFileIds")
        support.validateTaskAttachmentFiles(newAttachmentIds)

        task.update(
            title = command.title,
            description = command.description,
            attachments = newAttachmentIds.map { TaskAttachment(fileId = it) },
            expireAt = expireAt,
            validateExpireAt = task.type == TaskType.POST
        )
        val saved = support.saveTask(task)

        val newAttachmentSet = newAttachmentIds.toSet()
        val linked = newAttachmentSet.filter { !oldAttachmentIds.contains(it) }
        val removed = oldAttachmentIds.filter { !newAttachmentSet.contains(it) }
        support.updateFileStatuses(linked, UserFileStatus.LINKED)
        support.downgradeOrphanedFiles(removed)

        eventPublisher.publishEvent(TaskModifiedEvent(saved.id!!, groupIdObjectId, saved.relatedScheduleId))
        return support.toTaskDto(saved, groupIdObjectId)
    }
}
