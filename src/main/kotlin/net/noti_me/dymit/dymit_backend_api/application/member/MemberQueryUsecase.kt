package net.noti_me.dymit.dymit_backend_api.application.member

import net.noti_me.dymit.dymit_backend_api.application.member.dto.MemberQueryDto
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity

interface MemberQueryUsecase {

    fun getMemberById(id: String): MemberQueryDto
}