package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.DeleteSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 과제 제출 댓글 삭제 유즈케이스 구현체입니다.
 */
@Service
class DeleteSubmissionCommentUseCaseImpl(
    private val support: TaskServiceSupport
) : DeleteSubmissionCommentUseCase {

    override fun deleteSubmissionComment(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        submissionId: String,
        commentId: String
    ) {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        val submissionObjectId = TaskUseCaseObjectIdParser.parse(submissionId, "submissionId")
        support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)

        val comment = support.loadComment(commentId)
        if ( comment.taskId != task.id || comment.submissionId != submissionObjectId ) {
            throw BadRequestException(message = "댓글 대상이 올바르지 않습니다.")
        }

        comment.checkDeletePermission(memberId)
        support.deleteComment(comment.id!!)
    }
}
