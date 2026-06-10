package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskSubmissionCreatedEvent
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
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

    override fun createSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        command: CreateTaskSubmissionCommand
    ): TaskSubmissionDto {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        val member = support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        val taskObjectId = requireNotNull(task.id)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkSubmissionUpdatable(task)
        val assignee = support.requireTaskAssignee(taskObjectId, memberId)

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
        support.updateFileStatuses(fileIds, UserFileStatus.LINKED)
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
