package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.member.dto.UpdateNicknameCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import org.hibernate.validator.constraints.Length

// import org.jakarta.validation.constraints.NotBlank
// import org.jakarta.validation.constraints.Length

@Sanitize
@Schema(description = "회원 닉네임 수정 요청 객체")
data class MemberNicknameUpdateRequest(
    // @field: NotBlank(message = "Nickname cannot be blank")
    @field: Nickname
    @field: Length(min=1, max=20, message = "Nickname must be between 3 and 20 characters")
    val nickname: String,
) {

    fun toCommand(): UpdateNicknameCommand {
        return UpdateNicknameCommand(
            nickname = nickname,
        )
    }
}
