package net.noti_me.dymit.dymit_backend_api.member.application.dto

import net.noti_me.dymit.dymit_backend_api.member.application.port.`in`.web.dto.UpdateInterestsRequest

class UpdateInterestsCommand(
    val interests: List<String>
) {

    companion object {

        fun from(request: UpdateInterestsRequest): UpdateInterestsCommand {
            return UpdateInterestsCommand(
                interests = request.interests
            )
        }
    }
}