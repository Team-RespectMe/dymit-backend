package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.WithdrawCheckSubmissionByAssigneeUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssigneeStatus
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskSubmissionType
import org.springframework.stereotype.Service

/**
 * 체크형 과제 제출 철회 유즈케이스 구현체입니다.
 */
@Service
class WithdrawCheckSubmissionByAssigneeUseCaseImpl(
    private val support: TaskServiceSupport
) : WithdrawCheckSubmissionByAssigneeUseCase {

    override fun withdrawCheckSubmissionByAssignee(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        assigneeId: String
    ) {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val requesterMemberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        val assigneeMemberId = TaskUseCaseObjectIdParser.parse(assigneeId, "assigneeId")
        support.requireGroupMember(groupIdObjectId, requesterMemberId)

        if ( requesterMemberId != assigneeMemberId ) {
            throw ForbiddenException(message = "본인 제출 과제만 철회할 수 있습니다.")
        }

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.checkSubmissionUpdatable(task)

        if ( task.submissionType != TaskSubmissionType.CHECK ) {
            throw BadRequestException(message = "체크형 과제만 assigneeId 경로로 철회할 수 있습니다.")
        }

        val assignee = support.requireTaskAssignee(requireNotNull(task.id), assigneeMemberId)
        if ( assignee.status != TaskAssigneeStatus.SUBMITTED ) {
            throw NotFoundException(message = "존재하지 않는 제출입니다.")
        }

        assignee.markNotSubmitted()
        support.saveAssignee(assignee)
    }
}
