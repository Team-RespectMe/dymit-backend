package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.MemberPreview
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole

@Schema(
    description = "스터디 그룹 멤버 미리보기 응답 DTO",
)
data class GroupMemberPreviewResponse(
    @Schema(description = "스터디 그룹 멤버 ID")
    val memberId: String,
    @Schema(description = "스터디 그룹 멤버 닉네임")
    val nickname: String,
    @Schema(description = "스터디 그룹 멤버 역할")
    val role: GroupMemberRole,
    @Schema(description = "스터디 그룹 멤버 프로필 이미지")
    val profileImage: ProfileImageResponse
): BaseResponse() {

    companion object {
        fun from(dto: StudyGroupMemberQueryDto): GroupMemberPreviewResponse {
            return GroupMemberPreviewResponse(
                memberId = dto.memberId,
                nickname = dto.nickname,
                role = dto.role,
                profileImage = ProfileImageResponse.of(
                    type = dto.profileImage.type,
                    url = dto.profileImage.url
                )
            )
        }

        fun from(dto: MemberPreview): GroupMemberPreviewResponse {
            return GroupMemberPreviewResponse(
                memberId = dto.memberId,
                nickname = dto.nickname,
                role = dto.role,
                profileImage = ProfileImageResponse.of(
                    type = dto.profileImage.type,
                    url = dto.profileImage.url
                )
            )
        }
    }
}
