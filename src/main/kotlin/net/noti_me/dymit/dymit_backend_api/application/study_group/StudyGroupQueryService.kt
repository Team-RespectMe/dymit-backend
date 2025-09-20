package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.BlacklistDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.query.StudyGroupSummaryDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.BlackList

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

    fun getStudyGroup(
        memberInfo: MemberInfo,
        groupId: String
    ): StudyGroupQueryModelDto

    fun getStudyGroupMembers(
        memberInfo: MemberInfo,
        groupId: String
    ) : List<StudyGroupMemberQueryDto>

    fun getOwnedGroupCount(memberInfo: MemberInfo): Long

    fun getBlacklists(
        memberInfo: MemberInfo,
        groupId: String
    ): List<BlacklistDto>
}