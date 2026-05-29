package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmission
import net.noti_me.dymit.dymit_backend_api.ports.persistence.task.TaskSubmissionRepository
import org.springframework.stereotype.Service

/**
 * 과제 제출 생성 유즈케이스 구현체입니다.
 */
@Service
class CreateSubmissionUseCaseImpl(
    private val support: TaskServiceSupport,
    private val taskSubmissionRepository: TaskSubmissionRepository
) : CreateSubmissionUseCase {

    override fun createSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        command: CreateTaskSubmissionCommand
    ): TaskSubmissionDto {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkTaskActionAllowedBySchedule(task)
        support.checkSubmissionUpdatable(task)
        val assignee = support.requireTaskAssignee(task.id!!, memberId)

        if ( taskSubmissionRepository.findByTaskIdAndMemberId(task.id!!, memberId) != null ) {
            throw ConflictException(message = "이미 제출한 과제입니다.")
        }

        val submitAttachments = support.toSubmissionAttachments(command.attachments)
        val fileIds = support.submissionAttachmentFileIds(submitAttachments)
        support.validateSubmissionAttachmentFiles(fileIds)

        val saved = support.saveSubmission(
            TaskSubmission(
                taskId = task.id!!,
                memberId = memberId,
                title = command.title,
                content = command.content,
                attachments = submitAttachments
            )
        )

        assignee.markSubmitted()
        support.saveAssignee(assignee)
        support.updateFileStatuses(fileIds, UserFileStatus.LINKED)

        return support.toSubmissionDto(saved, groupIdObjectId)
    }
}
