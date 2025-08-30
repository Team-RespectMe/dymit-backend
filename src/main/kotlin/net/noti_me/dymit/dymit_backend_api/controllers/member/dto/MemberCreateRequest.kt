package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateCommand
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.multipart.MultipartFile

@Schema(description = "사용자 생성 요청 객체")
data class MemberCreateRequest(
    @Schema(description = "사용자 닉네임", required = true, example = "닉네임 입력")
    @field: Nickname
    @field: Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
    val nickname: String,
    @Schema(description = "OIDC 프로바이더", example = "GOOGLE", required = true)
    val oidcProvider: OidcProvider,
    @Schema(description = "Id token", required = true)
    @field: NotEmpty
    val idToken: String,
) {

    fun toCommand(): MemberCreateCommand {
        return MemberCreateCommand(
            nickname = nickname,
            oidcProvider = oidcProvider,
            idToken = idToken,
        )
    }
}