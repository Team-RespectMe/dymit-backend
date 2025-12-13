package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupCommandService
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupQueryService
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.EnlistBlacklistCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.StudyScheduleService
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/study-groups")
class StudyGroupController(
    private val studyGroupCommandService: StudyGroupCommandService,
    private val studyGroupQueryService: StudyGroupQueryService,
    private val studyGroupScheduleService: StudyScheduleService
): StudyGroupApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @RequestBody @Valid @Sanitize request: StudyGroupCreateRequest
    ): StudyGroupResponse {
        val result = studyGroupCommandService.createStudyGroup(
            member = memberInfo,
            command = request.toCommand()
        )

        return StudyGroupResponse.from(result)
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    override fun joinStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid @Sanitize request: StudyGroupJoinRequest
    ): StudyGroupMemberResponse {
        val result = studyGroupCommandService.joinStudyGroup(memberInfo, request.toCommand(groupId))
        return StudyGroupMemberResponse.from(result)
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    override fun searchStudyGroupByInviteCode(
        @LoginMember memberInfo: MemberInfo,
        @RequestParam("inviteCode", required=true) inviteCode: String
    ): StudyGroupResponse {
        val searchResult = studyGroupQueryService.getStudyGroupByInviteCode(
            memberInfo,
            inviteCode
        )
        return StudyGroupResponse.from(searchResult)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getMyStudyGroups(memberInfo: MemberInfo): ListResponse<StudyGroupListItemDto> {
        val studyGroups = studyGroupQueryService.getMyStudyGroups(memberInfo)
        studyGroupScheduleService.getUpcomingScheduleForGroups(groups = studyGroups)
        return ListResponse.from(studyGroups.map { StudyGroupListItemDto.from(it) })
    }

    @GetMapping("/{groupId}/invite-code")
    @ResponseStatus(HttpStatus.OK)
    override fun getStudyGroupInviteCode(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): InviteCodeResponse {
        val inviteCode = studyGroupQueryService.getInviteCode(memberInfo, groupId)
        return InviteCodeResponse(
            code =  inviteCode.code,
            createdAt = inviteCode.createdAt,
            expireAt = inviteCode.expireAt
        )
    }

    @GetMapping("/{groupId}")
    @ResponseStatus(HttpStatus.OK)
    override fun getStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): StudyGroupQueryDetailResponse {
        val group = studyGroupQueryService.getStudyGroup(memberInfo, groupId)
        val groupMembers = studyGroupQueryService.getStudyGroupMembers(memberInfo, groupId)
        val sorted = groupMembers.sortedBy { it.role  }
        return StudyGroupQueryDetailResponse.of(group, sorted)
    }

    @PutMapping("/{groupId}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.OK)
    override fun updateStudyGroupProfileImage(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @ModelAttribute @Valid @Sanitize request: UpdateStudyGroupProfileImageRequest
    ): StudyGroupResponse {
        val updatedGroup = studyGroupCommandService.updateStudyGroupProfileImage(
            member = memberInfo,
            command = request.toCommand(groupId)
        )

        return StudyGroupResponse.from(updatedGroup)
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ) {
        studyGroupCommandService.deleteStudyGroup(memberInfo, groupId)
    }

    @DeleteMapping("/{groupId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun leaveStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ) {
        studyGroupCommandService.leaveStudyGroup(memberInfo, groupId)
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun removeStudyGroupMember(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable memberId: String
    ) {
        studyGroupCommandService.expelStudyGroupMember(memberInfo, groupId, memberId)
    }

    @DeleteMapping("/{groupId}/blacklists")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun addStudyGroupMemberToBlacklist(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid @Sanitize request: BlackListEnlistRequest
    ) {
        val command = EnlistBlacklistCommand(
            groupId = groupId,
            targetMember = request.targetId,
            reason = request.reason
        )
        studyGroupCommandService.enlistBlacklist(memberInfo, command)
    }

    @GetMapping("/{groupId}/blacklists")
    @ResponseStatus(HttpStatus.OK)
    override fun getStudyGroupBlacklists(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): ListResponse<BlackListResponse> {
        val blacklists = studyGroupQueryService.getBlacklists(memberInfo = memberInfo,
            groupId = groupId)
            .asSequence()
            .map { BlackListResponse.from(it) }
            .toList()

        return ListResponse.from(blacklists)
    }

    @DeleteMapping("/{groupId}/blacklists/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun removeStudyGroupMemberFromBlacklist(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable memberId: String
    ) {
        studyGroupCommandService.delistBlacklist(memberInfo, groupId, memberId)
    }

    @PutMapping("/{groupId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid request: StudyGroupModifyRequest
    ): StudyGroupResponse {
        return StudyGroupResponse.from(
            studyGroupCommandService.updateStudyGroupInfo(
                memberInfo,
                request.toCommand(groupId)
            )
        )
    }

    @PatchMapping("/{groupId}/owner")
    @ResponseStatus(HttpStatus.OK)
    override fun changeStudyGroupOwner(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid request: ChangeStudyGroupOwnerRequest
    ) {
        studyGroupCommandService.changeStudyGroupOwner(
            memberInfo,
            request.toCommand(groupId)
        )
    }

    @GetMapping("/{groupId}/group-members")
    @ResponseStatus(HttpStatus.OK)
    override fun getStudyGroupMembers(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String

    ): ListResponse<StudyGroupMemberResponse> {
        return ListResponse.from(
               studyGroupQueryService.getStudyGroupMembers(memberInfo, groupId)
                 .map { StudyGroupMemberResponse.from(it) }
        )
    }
}
