package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupSummaryDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface StudyGroupQueryService {

    fun getStudyGroupByInviteCode(
        memberInfo: MemberInfo,
        inviteCode: String
    ): StudyGroupSummaryDto

    fun getMyStudyGroups(
        memberInfo: MemberInfo
    ): List<StudyGroupQueryModelDto>

    fun getInviteCode(
        memberInfo: MemberInfo,
        groupId: String
    ): InviteCodeVo
}