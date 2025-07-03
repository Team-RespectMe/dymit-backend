package net.noti_me.dymit.dymit_backend_api.domain.member

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "멤버 프로필 사진 객체")
class MemberProfileImageVo(
    @Schema(description = "프로필 사진 파일 경로", example = "/images/profile/testuser.jpg")
    val filePath: String = "",
    @Schema(description = "프로필 사진 URL", example = "https://example.com/images/profile/testuser.jpg")
    val url: String = "",
    @Schema(description = "프로필 사진 파일 크기 (바이트 단위)", example = "123456")
    val fileSize: Long = 0L,
    @Schema(description = "프로필 사진 너비 (픽셀 단위)", example = "200")
    val width: Int = 0,
    @Schema(description = "프로필 사진 높이 (픽셀 단위)", example = "200")
    val height: Int = 0,
) {

    @JsonIgnore
    fun isValid(): Boolean {
        return filePath.isNotEmpty() && url.isNotEmpty() && fileSize > 0 && width > 0 && height > 0
    }
}