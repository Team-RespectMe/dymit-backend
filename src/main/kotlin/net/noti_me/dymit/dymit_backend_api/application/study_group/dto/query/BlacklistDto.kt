package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.domain.study_group.BlackList

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