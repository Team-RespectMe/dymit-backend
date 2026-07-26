package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.dto.StudyGroupMemberData
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember

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
    val profileImage: ProfileImageVo
) {

    companion object {

        fun of(
            member: StudyGroupMemberData,
            role: GroupMemberRole
        ): MemberPreview {
            return MemberPreview(
                memberId = member.id,
                nickname = member.nickname,
                role = role,
                profileImage = ProfileImageVo.of(
                    type = member.profileImageType,
                    url = member.profileImageThumbnail
                )
            )
        }

        fun from(entity: StudyGroupMember): MemberPreview {
            return MemberPreview(
                memberId = entity.memberId.toHexString(),
                nickname = entity.nickname,
                role = entity.role,
                profileImage = entity.profileImage
            )
        }
    }
}
