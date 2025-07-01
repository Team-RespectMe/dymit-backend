package net.noti_me.dymit.dymit_backend_api.application.member.dto

import java.time.Instant

data class MemberCreateCommand(
    val nickname: String,
    val idToken: String 
)
