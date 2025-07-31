package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberProfileResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.ProfileImageResponse

@Schema(
    description = "스터디 그룹 멤버 미리보기 응답 DTO",
)
data class GroupMemberPreviewResponse(
    @Schema(description = "스터디 그룹 멤버 ID")
    val memberId: String,
    @Schema(description = "스터디 그룹 멤버 닉네임")
    val nickname: String,
    @Schema(description = "스터디 그룹 멤버 프로필 이미지")
    val profileImage: ProfileImageResponse
) {
}