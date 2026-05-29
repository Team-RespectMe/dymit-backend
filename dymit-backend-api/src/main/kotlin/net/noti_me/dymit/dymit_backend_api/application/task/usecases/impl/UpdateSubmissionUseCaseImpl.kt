package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.dto.UpdateTaskSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.UpdateSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.file.UserFileStatus
import org.springframework.stereotype.Service

/**
 * 과제 제출 수정 유즈케이스 구현체입니다.
 */
@Service
class UpdateSubmissionUseCaseImpl(
    private val support: TaskServiceSupport
) : UpdateSubmissionUseCase {

    override fun updateSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: UpdateTaskSubmissionCommand
    ): TaskSubmissionDto {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkTaskActionAllowedBySchedule(task)
        support.checkSubmissionUpdatable(task)
        support.requireTaskAssignee(task.id!!, memberId)

        val submission = support.loadSubmission(submissionId)
        if ( submission.taskId != task.id || submission.memberId != memberId ) {
            throw ForbiddenException(message = "본인 제출 과제만 수정할 수 있습니다.")
        }

        val oldFileIds = support.submissionAttachmentFileIds(submission.attachments).toSet()
        val updatedAttachments = support.toSubmissionAttachments(command.attachments)
        val newFileIds = support.submissionAttachmentFileIds(updatedAttachments)
        support.validateSubmissionAttachmentFiles(newFileIds)

        submission.update(
            title = command.title,
            content = command.content,
            attachments = updatedAttachments
        )
        val saved = support.saveSubmission(submission)

        val newFileSet = newFileIds.toSet()
        val linked = newFileSet.filter { !oldFileIds.contains(it) }
        val removed = oldFileIds.filter { !newFileSet.contains(it) }
        support.updateFileStatuses(linked, UserFileStatus.LINKED)
        support.downgradeOrphanedFiles(removed)

        return support.toSubmissionDto(saved, groupIdObjectId)
    }
}
