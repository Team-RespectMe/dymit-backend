package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.WithdrawSubmissionCommand
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.WithdrawSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import org.springframework.stereotype.Service

/**
 * 과제 제출 철회 유즈케이스 구현체입니다.
 */
@Service
class WithdrawSubmissionUseCaseImpl(
    private val support: TaskServiceSupport
) : WithdrawSubmissionUseCase {

    override fun execute(command: WithdrawSubmissionCommand) {
        val (memberInfo, groupId, taskId, submissionId) = command
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkSubmissionUpdatable(task)

        if ( task.submissionType == TaskSubmissionType.CHECK ) {
            throw BadRequestException(message = "체크형 과제 제출은 assigneeId 경로로만 철회할 수 있습니다.")
        }

        val assignee = support.requireTaskAssignee(task.id!!, memberId)
        val submission = support.loadSubmission(submissionId)

        if ( submission.taskId != task.id || submission.memberId != memberId ) {
            throw ForbiddenException(message = "본인 제출 과제만 철회할 수 있습니다.")
        }

        val fileIds = support.submissionAttachmentFileIds(submission.attachments)
        val submissionIdObjectId = requireNotNull(submission.id)
        support.removeCommentsBySubmission(submissionIdObjectId)
        support.removeSubmissionById(submissionIdObjectId)

        assignee.markNotSubmitted()
        support.saveAssignee(assignee)
        support.downgradeOrphanedFiles(fileIds)
    }
}
