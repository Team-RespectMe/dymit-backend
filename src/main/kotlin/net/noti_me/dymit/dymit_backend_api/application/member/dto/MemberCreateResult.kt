package net.noti_me.dymit.dymit_backend_api.application.member.dto

import java.time.Instant
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult

data class MemberCreateResult(
    val member: MemberDto,
    val loginResult: LoginResult
) {
    companion object {
        fun from(member: MemberDto, loginResult: LoginResult): MemberCreateResult {
            return MemberCreateResult(
                member = member,
                loginResult = loginResult
            )
        }
    }
}
