package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupCommandService
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupQueryService
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.ProfileImageUploadRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.InviteCodeResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupJoinRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupListItemDto
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupMemberResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupQueryDetailResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class StudyGroupController(
    private val studyGroupCommandService: StudyGroupCommandService,
    private val studyGroupQueryService: StudyGroupQueryService
): StudyGroupAPI {

    override fun createStudyGroup(
        memberInfo: MemberInfo,
        request: StudyGroupCreateRequest
    ): StudyGroupResponse {
        val result = studyGroupCommandService.createStudyGroup(
            member = memberInfo,
            command = request.toCommand()
        )

        return StudyGroupResponse.from(result)
    }

    override fun joinStudyGroup(
        memberInfo: MemberInfo,
        groupId: String,
        request: StudyGroupJoinRequest
    ): StudyGroupMemberResponse {
        val result = studyGroupCommandService.joinStudyGroup(memberInfo, request.toCommand(groupId))
        return StudyGroupMemberResponse.from(result)
    }

    override fun searchStudyGroupByInviteCode(
        memberInfo: MemberInfo,
        inviteCode: String
    ): StudyGroupResponse {
        val searchResult = studyGroupQueryService.getStudyGroupByInviteCode(
            memberInfo,
            inviteCode
        )
        return StudyGroupResponse.from(searchResult)
    }

    override fun getMyStudyGroups(memberInfo: MemberInfo): ListResponse<StudyGroupListItemDto> {
        val studyGroups = studyGroupQueryService.getMyStudyGroups(memberInfo)
        return ListResponse.from(studyGroups.map { StudyGroupListItemDto.from(it) })
    }

    override fun getStudyGroupInviteCode(
        memberInfo: MemberInfo,
        groupId: String
    ): InviteCodeResponse {
        val inviteCode = studyGroupQueryService.getInviteCode(memberInfo, groupId)
        return InviteCodeResponse(
            code =  inviteCode.code,
            createdAt = inviteCode.createdAt,
            expireAt = inviteCode.expireAt
        )
    }

    override fun getStudyGroup(memberInfo: MemberInfo, groupId: String)
    : StudyGroupQueryDetailResponse {
        val group = studyGroupQueryService.getStudyGroup(memberInfo, groupId)
        val groupMembers = studyGroupQueryService.getStudyGroupMembers(memberInfo, groupId)

        return StudyGroupQueryDetailResponse.of(group, groupMembers)
    }

    override fun updateStudyGroupProfileImage(
        memberInfo: MemberInfo,
        groupId: String,
        request: ProfileImageUploadRequest
    ): StudyGroupResponse {
        val updatedGroup = studyGroupCommandService.updateStudyGroupProfileImage(
            member = memberInfo,
            command = request.toGroupProfileUpdateCommand(groupId)
        )

        return StudyGroupResponse.from(updatedGroup)
    }

    override fun deleteStudyGroup(memberInfo: MemberInfo, groupId: String) {
        studyGroupCommandService.deleteStudyGroup(memberInfo, groupId)
    }

    override fun leaveStudyGroup(memberInfo: MemberInfo, groupId: String) {
        studyGroupCommandService.leaveStudyGroup(memberInfo, groupId)
    }

//    override fun getStudyGroupMembers(memberInfo: MemberInfo, groupId: String): ListResponse<StudyGroupMemberResponse> {
//
//    }
}