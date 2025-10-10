package net.noti_me.dymit.dymit_backend_api.controllers.common

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo

@Schema(description = "프로필 사진 응답 객체(사용자 프로필 또는 그룹 프로필 사진)")
class ProfileImageResponse(
    @Schema(description = "프로필 이미지 URL, type이 preset 인 경우 프리셋 번호가 주어집니다.", example = "https://example.com/images/profile/testuser.jpg")
    val url: String,
    @Schema(description = "프로필 이미지 타입", allowableValues = ["preset", "external"], example = "preset")
    val type: String = "preset",
): BaseResponse() {

    companion object {
        fun from(image: MemberProfileImageVo) : ProfileImageResponse {
            return ProfileImageResponse(
                url = image.url,
                type = image.type,
            )
        }

        fun from(profile: ProfileImageVo): ProfileImageResponse {
            return ProfileImageResponse(
                url = profile.url,
                type = profile.type,
            )
        }

        fun from(image: GroupProfileImageVo): ProfileImageResponse {
            return ProfileImageResponse(
                url = image.url,
                type = image.type,
            )
        }
    }
}