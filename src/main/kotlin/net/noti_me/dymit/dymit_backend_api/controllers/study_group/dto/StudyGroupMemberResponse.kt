package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import java.time.LocalDateTime

@Schema(description = "스터디 그룹 멤버 응답 DTO")
data class StudyGroupMemberResponse(
    @field:Schema(description = "스터디 그룹 ID")
    val groupId: String,
    @field:Schema(description = "스터디 그룹 멤버 ID")
    val memberId: String,
    @field:Schema(description = "스터디 그룹 멤버 닉네임")
    val nickname: String,
    @field:Schema(description = "스터디 그룹 멤버 역할")
    val role: GroupMemberRole,
    @field:Schema(description = "스터디 그룹 멤버 생성 시간")
    val createdAt: LocalDateTime
): BaseResponse() {

    companion object {
        fun from(dto: StudyGroupMemberDto): StudyGroupMemberResponse {
            return StudyGroupMemberResponse(
                groupId = dto.groupId,
                memberId = dto.memberId,
                nickname = dto.nickname,
                role = dto.role,
                createdAt = dto.createdAt ?: LocalDateTime.now()
            )
        }

        fun from(dto: StudyGroupMemberQueryDto): StudyGroupMemberResponse {
            return StudyGroupMemberResponse(
                groupId = dto.groupId,
                memberId = dto.memberId,
                nickname = dto.nickname,
                role = dto.role,
                createdAt = dto.createdAt ?: LocalDateTime.now()
            )
        }

    }
}