package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.member.dto.UpdateMemberProfileImageCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberPresetImage
import org.springframework.web.multipart.MultipartFile

@Schema(description = "프로필 이미지 업로드 요청 객체")
@Sanitize
class ProfileImageUploadRequest(
    @field: Schema(description = "프로필 이미지 타입")
    val type: ProfileImageType = ProfileImageType.PRESET,
    @field: Schema(description = "프리셋 이미지 사용 시 번호")
    val preset: MemberPresetImage? = null,
    @field: Schema(description = "프로필 이미지 파일", required = false)
    val file: MultipartFile? = null
) {

    fun toCommand(memberId: String) : UpdateMemberProfileImageCommand {
        return UpdateMemberProfileImageCommand(
            memberId = memberId,
            type = this.type,
            preset = this.preset,
            imageFile = this.file
        )
    }
}
