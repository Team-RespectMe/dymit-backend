package net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto

import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberProfileResponse

class GroupMemberSummaryResponse(
    val memberId: String,
    val nickname: String,
    val profileImage: MemberProfileResponse,
) {

}