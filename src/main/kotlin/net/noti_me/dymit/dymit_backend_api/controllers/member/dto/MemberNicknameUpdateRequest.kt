package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberNicknameUpdateCommand
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname

// import org.jakarta.validation.constraints.NotBlank
// import org.jakarta.validation.constraints.Length

data class MemberNicknameUpdateRequest(
    // @field: NotBlank(message = "Nickname cannot be blank")
    // @field: Length(min=3, max=20, message = "Nickname must be between 3 and 20 characters")
    @field: Nickname
    val nickname: String,
) {

    fun toCommand(): MemberNicknameUpdateCommand {
        return MemberNicknameUpdateCommand(
            nickname = nickname
        )
    }
}
