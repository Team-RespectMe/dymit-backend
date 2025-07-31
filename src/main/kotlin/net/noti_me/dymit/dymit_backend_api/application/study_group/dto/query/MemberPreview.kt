package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole

@Schema(
    description = "스터디 그룹 멤버 미리보기 DTO",
)
data class MemberPreview(
    @Schema(description = "회원 ID")
    val memberId: String,
    @Schema(description = "회원 닉네임")
    val nickname: String,
    @Schema(description = "그룹 내 회원 역할")
    val role: GroupMemberRole,
    @Schema(description = "회원 프로필 이미지")
    val profileImage: MemberProfileImageVo
) {
}