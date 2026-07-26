package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateSubmissionInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskCheckSubmissionDtoFactory
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.CreateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto.TaskFileStatusDto
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskSubmissionCreatedEvent
import net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.persistence.TaskSubmissionRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 과제 제출 생성 유즈케이스 구현체입니다.
 */
@Service
class CreateSubmissionUseCaseImpl(
    private val support: TaskServiceSupport,
    private val taskSubmissionRepository: TaskSubmissionRepository,
    private val eventPublisher: ApplicationEventPublisher
) : CreateSubmissionUseCase {

    override fun execute(input: CreateSubmissionInput): TaskSubmissionDto {
        val (memberInfo, groupId, taskId, command) = input
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        val member = support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        val taskObjectId = requireNotNull(task.id)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkSubmissionUpdatable(task)
        val assignee = support.requireTaskAssignee(taskObjectId, memberId)

        if ( task.submissionType == TaskSubmissionType.CHECK ) {
            if ( assignee.status == TaskAssigneeStatus.SUBMITTED ) {
                throw ConflictException(message = "이미 제출한 과제입니다.")
            }

            assignee.markSubmitted()
            val savedAssignee = support.saveAssignee(assignee)
            return TaskCheckSubmissionDtoFactory.from(task.identifier, savedAssignee, member)
        }

        if ( taskSubmissionRepository.findByTaskIdAndMemberId(taskObjectId, memberId) != null ) {
            throw ConflictException(message = "이미 제출한 과제입니다.")
        }

        val submitAttachments = support.toSubmissionAttachments(command.attachments)
        val fileIds = support.submissionAttachmentFileIds(submitAttachments)
        support.validateSubmissionAttachmentFiles(fileIds)

        val saved = support.saveSubmission(
            TaskSubmission(
                taskId = taskObjectId,
                memberId = memberId,
                title = command.title,
                content = command.content,
                attachments = submitAttachments
            )
        )

        assignee.markSubmitted()
        support.saveAssignee(assignee)
        support.updateFileStatuses(fileIds, TaskFileStatusDto.LINKED)
        eventPublisher.publishEvent(
            TaskSubmissionCreatedEvent(
                taskId = taskObjectId,
                groupId = groupIdObjectId,
                scheduleId = task.relatedScheduleId,
                task = task,
                group = support.loadGroup(groupId),
                member = member
            )
        )

        return support.toSubmissionDto(saved, groupIdObjectId)
    }
}
