package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberDto
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

@Schema(description = "회원 프로필 응답")
class MemberProfileResponse(
    @Schema(description = "회원 ID", example = "507f1f77bcf86cd799439011")
    val id: String,
    @Schema(description = "회원 닉네임", example = "testuser")
    val nickname: String,
    @Schema(description = "프로필 이미지 정보")
    val profileImage: ProfileImageResponse? = null,
    val oidcIdentities: List<OidcIdentity> = emptyList(),
) : BaseResponse() {

    companion object {

        fun from(member: MemberDto): MemberProfileResponse {
            return MemberProfileResponse(
                id = member.id,
                nickname = member.nickname,
                profileImage = member.profileImage?.let {
                    ProfileImageResponse(
                        url = it.url,
                        width = it.width,
                        height = it.height
                    )
                },
                oidcIdentities = member.oidcIdentities
            )
        }
    }
}
