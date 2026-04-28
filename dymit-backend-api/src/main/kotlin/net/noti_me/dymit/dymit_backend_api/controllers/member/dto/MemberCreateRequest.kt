package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import net.noti_me.dymit.dymit_backend_api.application.member.dto.CreateMemberCommand
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcProvider

@Schema(description = "사용자 생성 요청 객체")
@Sanitize
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
    @Schema(description = "관심 분야 리스트", example = "[\"스터디\"]", required = false)
    val interests: List<@NotBlank String> = emptyList(),
) {

    fun toCommand(): CreateMemberCommand {
        return CreateMemberCommand(
            nickname = nickname,
            oidcProvider = oidcProvider,
            idToken = idToken,
            interests = interests
        )
    }
}