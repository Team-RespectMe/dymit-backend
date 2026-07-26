package net.noti_me.dymit.dymit_backend_api.task.application

import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.GetTaskSubmissionQuery
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.task.application.TaskCheckSubmissionDtoFactory
import net.noti_me.dymit.dymit_backend_api.task.application.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.task.application.usecase.GetTaskSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskSubmissionType
import org.springframework.stereotype.Service

/**
 * 과제 제출 단건 조회 유즈케이스 구현체입니다.
 */
@Service
class GetTaskSubmissionUseCaseImpl(
    private val support: TaskServiceSupport
) : GetTaskSubmissionUseCase {

    override fun execute(query: GetTaskSubmissionQuery): TaskSubmissionDto {
        val (memberInfo, groupId, taskId, memberId) = query
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val requesterMemberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        val assigneeMemberId = TaskUseCaseObjectIdParser.parse(memberId, "assigneeId")

        support.requireGroupMember(groupIdObjectId, requesterMemberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        val assigneeMember = support.requireGroupMember(groupIdObjectId, assigneeMemberId)

        if ( task.submissionType == TaskSubmissionType.CHECK ) {
            val assignee = support.requireTaskAssignee(task.id!!, assigneeMemberId)
            if ( assignee.status != TaskAssigneeStatus.SUBMITTED ) {
                throw NotFoundException(message = "존재하지 않는 제출입니다.")
            }
            return TaskCheckSubmissionDtoFactory.from(task.identifier, assignee, assigneeMember)
        }

        val submission = support.loadSubmissionByTaskAndMember(task.id!!, assigneeMemberId)
        return support.toSubmissionDto(submission, groupIdObjectId)
    }
}
