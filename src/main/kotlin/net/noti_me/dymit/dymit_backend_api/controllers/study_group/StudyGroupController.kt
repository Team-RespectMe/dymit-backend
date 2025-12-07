package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupCommandService
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupQueryService
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.EnlistBlacklistCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleService
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.ProfileImageUploadRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.BlackListEnlistRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.BlackListResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.ChangeStudyGroupOwnerRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.InviteCodeResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupJoinRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupListItemDto
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupMemberResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupModifyRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupQueryDetailResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.UpdateStudyGroupProfileImageRequest
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class StudyGroupController(
    private val studyGroupCommandService: StudyGroupCommandService,
    private val studyGroupQueryService: StudyGroupQueryService,
    private val studyGroupScheduleService: StudyScheduleService
): StudyGroupApi {

    override fun createStudyGroup(
        memberInfo: MemberInfo,
        @RequestBody @Valid @Sanitize request: StudyGroupCreateRequest
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
        @Valid @Sanitize request: StudyGroupJoinRequest
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
        studyGroupScheduleService.getUpcomingScheduleForGroups(groups = studyGroups)
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
        val sorted = groupMembers.sortedBy { it.role  }
        return StudyGroupQueryDetailResponse.of(group, sorted)
    }

    override fun updateStudyGroupProfileImage(
        memberInfo: MemberInfo,
        groupId: String,
        @Valid @Sanitize request: UpdateStudyGroupProfileImageRequest
    ): StudyGroupResponse {
        val updatedGroup = studyGroupCommandService.updateStudyGroupProfileImage(
            member = memberInfo,
            command = request.toCommand(groupId)
        )

        return StudyGroupResponse.from(updatedGroup)
    }

    override fun deleteStudyGroup(memberInfo: MemberInfo, groupId: String) {
        studyGroupCommandService.deleteStudyGroup(memberInfo, groupId)
    }

    override fun leaveStudyGroup(memberInfo: MemberInfo, groupId: String) {
        studyGroupCommandService.leaveStudyGroup(memberInfo, groupId)
    }

    override fun removeStudyGroupMember(memberInfo: MemberInfo, groupId: String, memberId: String) {
        studyGroupCommandService.expelStudyGroupMember(memberInfo, groupId, memberId)
    }

    override fun addStudyGroupMemberToBlacklist(
        memberInfo: MemberInfo,
        groupId: String,
        @Valid @Sanitize request: BlackListEnlistRequest
    ) {
        val command = EnlistBlacklistCommand(
            groupId = groupId,
            targetMember = request.targetId,
            reason = request.reason
        )
        studyGroupCommandService.enlistBlacklist(memberInfo, command)
    }

    override fun getStudyGroupBlacklists(
        memberInfo: MemberInfo,
        groupId: String
    ): ListResponse<BlackListResponse> {
        val blacklists = studyGroupQueryService.getBlacklists(memberInfo = memberInfo,
            groupId = groupId)
            .asSequence()
            .map { BlackListResponse.from(it) }
            .toList()

        return ListResponse.from(blacklists)
    }

    override fun removeStudyGroupMemberFromBlacklist(
        memberInfo: MemberInfo,
        groupId: String,
        memberId: String
    ) {
        studyGroupCommandService.delistBlacklist(memberInfo, groupId, memberId)
    }

    override fun updateStudyGroup(
        memberInfo: MemberInfo,
        groupId: String,
        request: StudyGroupModifyRequest
    ): StudyGroupResponse {
        return StudyGroupResponse.from(
            studyGroupCommandService.updateStudyGroupInfo(
                memberInfo,
                request.toCommand(groupId)
            )
        )
    }

    override fun changeStudyGroupOwner(
        memberInfo: MemberInfo,
        groupId: String,
        request: ChangeStudyGroupOwnerRequest
    ) {
        studyGroupCommandService.changeStudyGroupOwner(
            memberInfo,
            request.toCommand(groupId)
        )
    }

   override fun getStudyGroupMembers(memberInfo: MemberInfo, groupId: String): ListResponse<StudyGroupMemberResponse> {
       return ListResponse.from(
              studyGroupQueryService.getStudyGroupMembers(memberInfo, groupId)
                .map { StudyGroupMemberResponse.from(it) }
       )
   }
}
