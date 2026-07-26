package net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.task.domain.TaskProfileImageType

@Schema(description = "과제 멤버 정보")
class MemberVo(
    @field:Schema(description = "멤버 ID")
    val memberId: String,
    @field:Schema(description = "닉네임")
    val nickname: String,
    @field:Schema(description = "프로필 이미지 URL")
    val profileImageUrl: String,
    @field:Schema(description = "프로필 이미지 타입")
    val profileImageType: TaskProfileImageType
)
