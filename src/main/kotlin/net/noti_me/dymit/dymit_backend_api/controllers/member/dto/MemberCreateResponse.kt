package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult

@Schema(description = "멤버 생성 요청 응답 객체")
class MemberCreateResponse(
    @Schema(description = "생성된 멤버 프로필 정보")
    val member: MemberProfileResponse,
    val loginResult: LoginResult
) {

    companion object {
        fun from(result: MemberCreateResult): MemberCreateResponse {
            return MemberCreateResponse(
                member = MemberProfileResponse.from(result.member),
                loginResult = result.loginResult
            )
        }
    }
}