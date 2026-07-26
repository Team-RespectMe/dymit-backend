package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.BlacklistDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupMemberQueryDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupQueryModelDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.query.StudyGroupSummaryDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.BlackList

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