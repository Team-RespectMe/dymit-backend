package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateTaskUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAttachment
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskType
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskCreatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 과제 생성 유즈케이스 구현체입니다.
 */
@Service
class CreateTaskUseCaseImpl(
    private val support: TaskServiceSupport,
    private val eventPublisher: ApplicationEventPublisher
) : CreateTaskUseCase {

    override fun createTask(memberInfo: MemberInfo, groupId: String, command: CreateTaskCommand): TaskDto {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val group = support.loadGroup(groupId)
        support.checkOwner(memberInfo, group)

        val schedule = support.loadSchedule(command.relatedScheduleId)
        val scheduleId = requireNotNull(schedule.id)
        support.checkScheduleInGroup(schedule, groupIdObjectId)
        val requestedAt = LocalDateTime.now()
        val resolvedType = support.resolveTaskTypeBySchedule(schedule, requestedAt)
        if ( resolvedType == TaskType.PRE ) {
            support.validatePreTaskCreatable(schedule, requestedAt)
        }
        val expireAt = support.normalizeExpireAtForCreate(resolvedType, command.expireAt, schedule)

        val attachmentIds = support.toObjectIds(command.attachmentFileIds.distinct(), "attachmentFileIds")
        val assigneeMemberIds = support.toObjectIds(command.assigneeMemberIds.distinct(), "assigneeMemberIds")
        support.validateTaskAttachmentFiles(attachmentIds)
        if ( resolvedType != TaskType.PRE ) {
            support.validateAssigneeMembersInGroup(groupIdObjectId, assigneeMemberIds)
        }

        val saved = support.saveTask(
            Task(
                relatedScheduleId = scheduleId,
                type = resolvedType,
                title = command.title,
                description = command.description,
                attachments = attachmentIds.map { TaskAttachment(fileId = it) },
                expireAt = expireAt,
                submissionType = command.submissionType
            )
        )
        val savedTaskId = requireNotNull(saved.id)

        if ( saved.type == TaskType.PRE ) {
            support.initializeAssigneesForPreTask(savedTaskId, scheduleId)
        } else {
            support.initializeAssignees(savedTaskId, assigneeMemberIds)
        }

        support.updateFileStatuses(attachmentIds, UserFileStatus.LINKED)
        eventPublisher.publishEvent(
            TaskCreatedEvent(
                taskId = savedTaskId,
                groupId = groupIdObjectId,
                scheduleId = scheduleId,
                task = saved,
                group = group
            )
        )
        return support.toTaskDto(saved, groupIdObjectId)
    }
}
