package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberProfileImageCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupImageUpdateCommand
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.springframework.web.multipart.MultipartFile

@Schema(description = "프로필 이미지 업로드 요청 객체")
class ProfileImageUploadRequest(
    @field: Schema(description = "프로필 이미지 타입", allowableValues = ["preset", "external"], example = "preset")
    val type: ProfileImageType = ProfileImageType.PRESET,
    @field: Schema(description = "프리셋 이미지 사용 시 번호")
    val presetNo : Int? = null,
    @field: Schema(description = "프로필 이미지 파일", required = false)
    val file: MultipartFile? = null
) {

    fun toCommand() : MemberProfileImageCommand {
        return MemberProfileImageCommand(
            type = type,
            presetNo = presetNo,
            imageFile = file
        )
    }

    fun toGroupProfileUpdateCommand(
        groupId: String
    ): StudyGroupImageUpdateCommand {
        return StudyGroupImageUpdateCommand(
            groupId = groupId,
            type = type,
            value = presetNo,
            file = file
        )
    }
}