package net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query

import net.noti_me.dymit.dymit_backend_api.study_group.domain.BlackList

class BlacklistDto(
    val id: String,
    val nickname: String,
    val reason: String
) {

    companion object {
        fun from(blacklist: BlackList): BlacklistDto {
            return BlacklistDto(
                id = blacklist.memberId.toHexString(),
                nickname = blacklist.nickname,
                reason = blacklist.reason
            )
        }
    }
}