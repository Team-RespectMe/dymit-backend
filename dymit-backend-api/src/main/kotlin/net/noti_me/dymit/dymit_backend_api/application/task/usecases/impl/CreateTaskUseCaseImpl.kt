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
        support.checkScheduleInGroup(schedule, groupIdObjectId)
        support.validateTaskTypeWithSchedule(command.type, schedule)
        val expireAt = support.normalizeExpireAtForCreate(command.type, command.expireAt, schedule)

        val attachmentIds = support.toObjectIds(command.attachmentFileIds.distinct(), "attachmentFileIds")
        support.validateTaskAttachmentFiles(attachmentIds)

        val saved = support.saveTask(
            Task(
                relatedScheduleId = schedule.id!!,
                type = command.type,
                title = command.title,
                description = command.description,
                attachments = attachmentIds.map { TaskAttachment(fileId = it) },
                expireAt = expireAt
            )
        )

        if ( saved.type == TaskType.PRE ) {
            support.initializeAssigneesForPreTask(saved.id!!, schedule.id!!)
        }

        support.updateFileStatuses(attachmentIds, UserFileStatus.LINKED)
        eventPublisher.publishEvent(TaskCreatedEvent(saved.id!!, groupIdObjectId, schedule.id!!))
        return support.toTaskDto(saved, groupIdObjectId)
    }
}
