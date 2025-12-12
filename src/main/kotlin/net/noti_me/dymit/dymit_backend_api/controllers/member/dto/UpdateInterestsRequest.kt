package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import net.noti_me.dymit.dymit_backend_api.application.member.dto.UpdateInterestsCommand

@Schema(description = "회원 관심사 변경 요청 객체")
data class UpdateInterestsRequest(
    @field: Schema(description = "관심사 목록", example = "[\"스터디\", \"개발\"]")
    @field: Size(min = 1, message = "관심사는 최소 1개 이상 선택해야합니다.")
    val interests: List<@NotEmpty(message = "관심사는 공백문자를 포함하거나 비어있을 수 없습니다.")String>
) {

    fun toCommand(): UpdateInterestsCommand {
        return UpdateInterestsCommand(
            interests = interests
        )
    }
}