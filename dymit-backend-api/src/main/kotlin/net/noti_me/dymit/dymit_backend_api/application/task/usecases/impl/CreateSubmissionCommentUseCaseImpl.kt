package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionComment
import org.springframework.stereotype.Service

/**
 * 과제 제출 댓글 생성 유즈케이스 구현체입니다.
 */
@Service
class CreateSubmissionCommentUseCaseImpl(
    private val support: TaskServiceSupport
) : CreateSubmissionCommentUseCase {

    override fun createSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        command: CreateTaskSubmissionCommentCommand
    ): TaskSubmissionCommentDto {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.requireTaskAssignee(task.id!!, memberId)

        val submission = support.loadSubmission(submissionId)
        if ( submission.taskId != task.id ) {
            throw BadRequestException(message = "과제 제출과 댓글 대상이 일치하지 않습니다.")
        }

        val saved = support.saveComment(
            TaskSubmissionComment(
                taskId = task.id!!,
                submissionId = submission.id!!,
                writerId = memberId,
                content = command.content
            )
        )
        return support.toCommentDto(saved, groupIdObjectId)
    }
}
