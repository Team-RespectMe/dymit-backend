package net.noti_me.dymit.dymit_backend_api.member.application.dto

data class MemberCreateResult(
    val member: MemberDto,
    val loginResult: MemberAuthenticationResultDto
) {
    companion object {
        fun from(
            member: MemberDto,
            loginResult: MemberAuthenticationResultDto
        ): MemberCreateResult {
            return MemberCreateResult(
                member = member,
                loginResult = loginResult
            )
        }
    }
}

data class MemberAuthenticationResultDto(
    val memberId: String,
    val accessToken: String,
    val refreshToken: String
)
