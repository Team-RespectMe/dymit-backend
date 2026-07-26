package net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberProfileImageType
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberProfileImageVo

/**
 * 회원 프로필 사진 응답 객체입니다.
 */
@Schema(description = "프로필 사진 응답 객체(사용자 프로필 또는 그룹 프로필 사진)")
class ProfileImageResponse(
    @Schema(description = "프로필 이미지 URL, type이 preset 인 경우 프리셋 번호가 주어집니다.", example = "https://example.com/images/profile/testuser.jpg")
    val url: String,
    @Schema(description = "프로필 이미지 타입", allowableValues = ["PRESET", "EXTERNAL"], example = "PRESET")
    val type: MemberProfileImageType,
): BaseResponse() {

    companion object {
        /**
         * 회원 프로필 이미지 값으로 응답을 생성합니다.
         */
        fun from(image: MemberProfileImageVo) : ProfileImageResponse {
            return ProfileImageResponse(
                url = image.thumbnail,
                type = image.type,
            )
        }
    }
}
