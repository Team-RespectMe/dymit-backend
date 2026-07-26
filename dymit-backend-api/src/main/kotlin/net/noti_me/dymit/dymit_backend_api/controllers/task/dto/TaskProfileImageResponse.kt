package net.noti_me.dymit.dymit_backend_api.controllers.task.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.domain.task.TaskProfileImageType

/**
 * 과제 참여자 프로필 이미지 응답입니다.
 */
@Schema(description = "과제 참여자 프로필 이미지 응답")
class TaskProfileImageResponse(
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/profile/testuser.jpg")
    val url: String,
    @Schema(description = "프로필 이미지 타입", allowableValues = ["PRESET", "EXTERNAL"], example = "PRESET")
    val type: TaskProfileImageType,
) : BaseResponse() {

    companion object {
        /**
         * 프로필 이미지 값으로 응답을 생성합니다.
         */
        fun of(
            type: TaskProfileImageType,
            url: String
        ): TaskProfileImageResponse {
            return TaskProfileImageResponse(
                url = url,
                type = type,
            )
        }
    }
}
