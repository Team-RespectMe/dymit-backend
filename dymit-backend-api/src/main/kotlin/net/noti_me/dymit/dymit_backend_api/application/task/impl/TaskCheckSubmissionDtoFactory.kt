package net.noti_me.dymit.dymit_backend_api.application.task.impl

import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskSubmissionDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskAssignee

/**
 * 체크형 과제 제출 응답 DTO 생성기입니다.
 */
object TaskCheckSubmissionDtoFactory {

    /**
     * 체크형 과제의 제출 상태를 제출 응답 DTO로 변환합니다.
     *
     * @param taskId 과제 ID
     * @param assignee 과제 대상자 엔티티
     * @param member 스터디 그룹 멤버 엔티티
     * @return 체크형 제출 응답 DTO
     */
    fun from(taskId: String, assignee: TaskAssignee, member: StudyGroupMember): TaskSubmissionDto {
        return TaskSubmissionDto(
            submissionId = assignee.identifier,
            taskId = taskId,
            memberId = member.memberId.toHexString(),
            memberNickname = member.nickname,
            memberProfileImageUrl = member.profileImage.url,
            memberProfileImageType = member.profileImage.type,
            title = "",
            content = "",
            attachments = emptyList(),
            createdAt = assignee.updatedAt ?: assignee.createdAt
        )
    }
}
