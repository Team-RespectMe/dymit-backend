package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import java.time.Instant

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
    val createdAt: Instant
): BaseResponse() {

    companion object {
        fun from(dto: StudyGroupMemberDto): StudyGroupMemberResponse {
            return StudyGroupMemberResponse(
                groupId = dto.groupId,
                memberId = dto.memberId,
                nickname = dto.nickname,
                role = dto.role,
                createdAt = dto.createdAt
            )
        }

        fun from(dto: StudyGroupMemberQueryDto): StudyGroupMemberResponse {
            return StudyGroupMemberResponse(
                groupId = dto.groupId,
                memberId = dto.memberId,
                nickname = dto.nickname,
                role = dto.role,
                createdAt = dto.createdAt
            )
        }

    }
}
