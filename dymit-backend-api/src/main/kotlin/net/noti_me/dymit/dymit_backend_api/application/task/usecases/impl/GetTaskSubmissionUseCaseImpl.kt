package net.noti_me.dymit.dymit_backend_api.application.task.usecases.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.application.task.impl.TaskServiceSupport
import net.noti_me.dymit.dymit_backend_api.application.task.usecases.GetTaskSubmissionUseCase
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service

/**
 * 과제 제출 단건 조회 유즈케이스 구현체입니다.
 */
@Service
class GetTaskSubmissionUseCaseImpl(
    private val support: TaskServiceSupport
) : GetTaskSubmissionUseCase {

    override fun getTaskSubmission(
        memberInfo: MemberInfo,
        groupId: String,
        taskId: String,
        memberId: String
    ): TaskSubmissionDto {
        val groupIdObjectId = TaskUseCaseObjectIdParser.parse(groupId, "groupId")
        val requesterMemberId = TaskUseCaseObjectIdParser.parse(memberInfo.memberId, "memberId")
        val assigneeMemberId = TaskUseCaseObjectIdParser.parse(memberId, "memberId")

        support.requireGroupMember(groupIdObjectId, requesterMemberId)

        val task = support.loadTask(taskId)
        support.checkTaskInGroup(task, groupIdObjectId)
        support.requireGroupMember(groupIdObjectId, assigneeMemberId)

        val submission = support.loadSubmissionByTaskAndMember(task.id!!, assigneeMemberId)
        return support.toSubmissionDto(submission, groupIdObjectId)
    }
}
