package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.common.response.BaseResponse

class MemberProfileResponse(
    val id: String,
    val nickname: String,
    val profileImage: MemberProfileImageVo? = null,
) : BaseResponse() {
    companion object {

        fun default() : MemberProfileResponse {
            return MemberProfileResponse(
                id = "not implemented yet",
                nickname = "not imeplemented yet",
                profileImage = MemberProfileImageVo(
                    filePath = "not implemented yet",
                    url = "not implemented yet",
                    fileSize = 0L,
                    width = 0,
                    height = 0
                )
            )
        }
    }
}
