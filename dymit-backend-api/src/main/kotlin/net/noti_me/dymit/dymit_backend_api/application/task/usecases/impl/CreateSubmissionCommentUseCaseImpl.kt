package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskSubmissionCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.CreateSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionComment
import net.noti_me.dymit.dymit_backend_api.domain.task.event.TaskSubmissionCommentCreatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

/**
 * 과제 제출 댓글 생성 유즈케이스 구현체입니다.
 */
@Service
class CreateSubmissionCommentUseCaseImpl(
    private val support: TaskServiceSupport,
    private val eventPublisher: ApplicationEventPublisher
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
        val member = support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        val taskObjectId = requireNotNull(task.id)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.requireTaskAssignee(taskObjectId, memberId)

        val submission = support.loadSubmission(submissionId)
        val submissionObjectId = requireNotNull(submission.id)
        if ( submission.taskId != task.id ) {
            throw BadRequestException(message = "과제 제출과 댓글 대상이 일치하지 않습니다.")
        }

        val saved = support.saveComment(
            TaskSubmissionComment(
                taskId = taskObjectId,
                submissionId = submissionObjectId,
                writerId = memberId,
                content = command.content
            )
        )

        if ( submission.memberId != memberId ) {
            eventPublisher.publishEvent(
                TaskSubmissionCommentCreatedEvent(
                    taskId = taskObjectId,
                    groupId = groupIdObjectId,
                    submissionId = submissionObjectId,
                    assigneeMemberId = submission.memberId,
                    task = task,
                    group = support.loadGroup(groupId),
                    member = member
                )
            )
        }

        return support.toCommentDto(saved, groupIdObjectId)
    }
}
