package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType

@Schema(description = "프로필 사진 응답 객체(사용자 프로필 또는 그룹 프로필 사진)")
class ProfileImageResponse(
    @Schema(
        description = "프로필 이미지 URL, type이 preset 인 경우 프리셋 번호가 주어집니다.",
        example = "https://example.com/images/profile/testuser.jpg"
    )
    val url: String,
    @Schema(
        description = "프로필 이미지 타입",
        allowableValues = ["PRESET", "EXTERNAL"],
        example = "PRESET"
    )
    val type: ProfileImageType
) : BaseResponse() {

    companion object {
        fun of(type: ProfileImageType, url: String) =
            ProfileImageResponse(url = url, type = type)
    }
}
