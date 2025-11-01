package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupImageUpdateCommand
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupPresetImage
import org.springframework.web.multipart.MultipartFile

@Schema(description = "스터디 그룹 프로필 이미지 업데이트 요청 객체")
data class UpdateStudyGroupProfileImageRequest(
    @field: Schema(description = "프로필 이미지 타입")
    val type: ProfileImageType,
    @field: Schema(description = "프리셋 이미지 사용 시 번호")
    val preset: GroupPresetImage? = null,
    @field: Schema(description = "프로필 이미지 파일", required = false)
    val file: MultipartFile? = null
) {

    fun toCommand(groupId: String): StudyGroupImageUpdateCommand {
        return StudyGroupImageUpdateCommand(
            groupId = groupId,
            type = type,
            value = preset,
            file = file
        )
    }
}

