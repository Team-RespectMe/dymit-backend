package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.CreateSubmissionCommentInput
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionCommentDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.CreateSubmissionCommentUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionComment
import net.noti_me.dymit.dymit_backend_api.task.domain.event.TaskSubmissionCommentCreatedEvent
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

    override fun execute(input: CreateSubmissionCommentInput): TaskSubmissionCommentDto {
        val (memberInfo, groupId, taskId, submissionId, command) = input
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val memberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        val member = support.requireGroupMember(groupIdObjectId, memberId)

        val task = support.loadTask(taskId)
        val taskObjectId = requireNotNull(task.id)
        support.checkTaskInGroup(task, groupIdObjectId)

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
