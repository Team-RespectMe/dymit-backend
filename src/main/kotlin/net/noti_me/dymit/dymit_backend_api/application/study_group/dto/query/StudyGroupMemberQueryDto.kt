package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query

import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo

class StudyGroupMemberQueryDto(
    val memberId: String,
    val nickname: String,
    var profileImage: MemberProfileImageVo
) {
}