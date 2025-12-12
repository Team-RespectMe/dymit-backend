package net.noti_me.dymit.dymit_backend_api.application.member.dto

import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.UpdateInterestsRequest

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