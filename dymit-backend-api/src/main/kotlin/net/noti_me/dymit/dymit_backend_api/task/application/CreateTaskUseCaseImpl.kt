package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateTaskInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskExpireAtNormalizer
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.CreateTaskUseCase
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAttachment
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskType
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskCreatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 과제 생성 유즈케이스 구현체입니다.
 */
@Service
class CreateTaskUseCaseImpl(
    private val support: TaskServiceSupport,
    private val eventPublisher: ApplicationEventPublisher
) : CreateTaskUseCase {

    override fun execute(input: CreateTaskInput): TaskDto {
        val (memberInfo, groupId, command) = input
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val group = support.loadGroup(groupId)
        support.checkOwner(memberInfo, group)

        val schedule = support.loadSchedule(command.relatedScheduleId)
        val scheduleId = requireNotNull(schedule.id)
        support.checkScheduleInGroup(schedule, groupIdObjectId)
        val requestedAt = TaskExpireAtNormalizer.currentUtcDateTime()
        val resolvedType = support.resolveTaskTypeBySchedule(schedule, requestedAt)
        if ( resolvedType == TaskType.PRE ) {
            support.validatePreTaskCreatable(schedule, requestedAt)
        }
        val expireAt = support.normalizeExpireAtForCreate(resolvedType, command.expireAt, schedule)

        val attachmentIds = support.toObjectIds(command.attachmentFileIds.distinct(), "attachmentFileIds")
        val assigneeMemberIds = if ( resolvedType == TaskType.POST ) {
            support.toObjectIds(command.assigneeMemberIds.distinct(), "assigneeMemberIds")
        } else {
            emptyList()
        }
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

        support.updateFileStatuses(attachmentIds, TaskFileStatusDto.LINKED)
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
