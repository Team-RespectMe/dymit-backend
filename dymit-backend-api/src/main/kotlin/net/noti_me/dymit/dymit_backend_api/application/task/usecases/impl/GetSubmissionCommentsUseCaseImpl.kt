package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetSubmissionCommentsUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 과제 제출 댓글 목록 조회 유즈케이스 구현체입니다.
 */
@Service
class GetSubmissionCommentsUseCaseImpl(
    private val support: TaskServiceSupport
) : GetSubmissionCommentsUseCase {

    override fun getSubmissionComments(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String
    ): List<TaskSubmissionCommentDto> {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")

        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)

        val submission = support.loadSubmission(submissionId)
        if ( submission.taskId != task.id ) {
            throw BadRequestException(message = "과제 제출 조회 대상이 올바르지 않습니다.")
        }

        return support.loadCommentsBySubmission(submission.id!!)
            .map { support.toCommentDto(it, groupIdObjectId) }
    }
}
