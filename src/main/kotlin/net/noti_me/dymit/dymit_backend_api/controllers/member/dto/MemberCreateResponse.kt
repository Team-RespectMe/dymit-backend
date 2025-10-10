package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberCreateResult
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse
import net.noti_me.dymit.dymit_backend_api.controllers.common.ProfileImageResponse

@Schema(description = "멤버 생성 요청 응답 객체")
class MemberCreateResponse(
    val memberId: String,
    val nickname: String,
    val profileImage: ProfileImageResponse? = null,
    val accessToken: String = "",
    val refreshToken: String = ""
): BaseResponse() {

    companion object {
        fun from(result: MemberCreateResult): MemberCreateResponse {
            return MemberCreateResponse(
                memberId = result.member.id,
                nickname = result.member.nickname,
                profileImage = result.member.profileImage?.let {
                    ProfileImageResponse.from(it)
                },
                accessToken = result.loginResult.accessToken,
                refreshToken = result.loginResult.refreshToken
            )
        }
    }
}