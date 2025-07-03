package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "프로필 사진 응답 객체(사용자 프로필 또는 그룹 프로필 사진)")
class ProfileImageResponse(
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/profile/testuser.jpg")
    val url: String,
    @Schema(description = "프로필 이미지 너비", example = "200")
    val width: Int,
    @Schema(description = "프로필 이미지 높이", example = "200")
    val height: Int
) {
}