package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole

@Schema(description = "스터디 그룹 멤버 응답 DTO")
data class StudyGroupMemberResponse(
    val groupId: String,
    val memberId: String,
    val nickname: String,
    val role: GroupMemberRole,
    val createdAt: String,
): BaseResponse() {

    companion object {
        fun from(dto: StudyGroupMemberDto): StudyGroupMemberResponse {
            return StudyGroupMemberResponse(
                groupId = dto.groupId,
                memberId = dto.memberId,
                nickname = dto.nickname,
                role = dto.role,
                createdAt = dto.createdAt.toString()
            )
        }
    }
}