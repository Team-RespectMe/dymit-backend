package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
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
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "스터디 그룹 API", description = "스터디 그룹 관련 API")
@RequestMapping("/api/v1/study-groups")
interface StudyGroupApi {

    /**
     * 스터디 그룹 생성 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param request 스터디 그룹 생성 요청 정보
     * @return 생성된 스터디 그룹의 정보
     */
    @Operation(summary = "스터디 그룹 생성 API", description = "로그인한 멤버가 새로운 스터디 그룹을 생성합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "스터디 그룹 생성 성공"),
    ])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name="bearer-jwt")
    fun createStudyGroup(
        @LoginMember
        memberInfo: MemberInfo,
        @RequestBody
        @Valid
        request: StudyGroupCreateRequest
    ): StudyGroupResponse

    /**
     * 스터디 그룹 가입 요청
     */
    @Operation(summary = "스터디 그룹 가입 API", description = "스터디 그룹에 가입합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "가입 성공"),
    ])
    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements(SecurityRequirement(name = "bearer-jwt"))
    fun joinStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid request: StudyGroupJoinRequest
    ): StudyGroupMemberResponse

    /**
     * 스터디 그룹 검색 API
     */
    @Operation(summary = "초대 코드를 통한 스터디 그룹 검색 API", description = "초대 코드에 따른 스터디 그룹을 검색합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "검색 성공"),
    ])
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirements(SecurityRequirement(name = "bearer-jwt"))
    fun searchStudyGroupByInviteCode(
        @LoginMember memberInfo: MemberInfo,
        @RequestParam("inviteCode", required=true) inviteCode: String
    ): StudyGroupResponse

    /**
     * 로그인 한 사용자가 속한 스터디 그룹 목록을 반환합니다.
     */
    @Operation(summary = "내 스터디 그룹 목록 조회 API", description = "로그인한 사용자가 속한 스터디 그룹 목록을 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공"),
    ])
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun getMyStudyGroups(
        @LoginMember memberInfo: MemberInfo
    ): ListResponse<StudyGroupListItemDto>

    /**
     * 스터디 그룹의 Invite Code를 조회합니다.
     */
    @Operation(summary = "스터디 그룹 Invite Code 조회 API", description = "스터디 그룹의 Invite Code를 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공"),
    ])
    @GetMapping("/{groupId}/invite-code")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun getStudyGroupInviteCode(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): InviteCodeResponse

    /**
     * 스터디 그룹 세부 조회 API
     */
    @Operation(summary = "스터디 그룹 세부 조회 API", description = "스터디 그룹의 세부 정보를 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공"),
    ])
    @GetMapping("/{groupId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun getStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): StudyGroupQueryDetailResponse

    /**
     * 스터디 그룹 프로필 이미지 업데이트 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param StudyGroupImageUpdateRequest 스터디 그룹 이미지 업데이트 요청 정보
     */
    @Operation(summary = "스터디 그룹 프로필 이미지 업데이트 API", description = "스터디 그룹의 프로필 이미지를 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 이미지 업데이트 성공")
    @PutMapping("/{groupId}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun updateStudyGroupProfileImage(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @Valid @ModelAttribute request: UpdateStudyGroupProfileImageRequest
    ): StudyGroupResponse

    /**
     * 스터디 그룹 삭제 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     */
    @Operation(summary = "스터디 그룹 삭제 API", description = "스터디 그룹을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 삭제 성공")
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer-jwt")
    fun deleteStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    )

    /**
     * 스터디 그룹 탈퇴 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     */
    @Operation(summary = "스터디 그룹 탈퇴 API", description = "스터디 그룹에서 탈퇴합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 탈퇴 성공")
    @DeleteMapping("/{groupId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer-jwt")
    fun leaveStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): Unit

    /**
     * 스터디 그룹 멤버 퇴장 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param memberId 강퇴할 멤버 ID
     */
    @Operation(summary = "스터디 그룹 멤버 삭제 API", description = "스터디 그룹에서 탈퇴할 때 사용합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 멤버 강퇴 성공")
    @DeleteMapping("/{groupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer-jwt")
    fun removeStudyGroupMember(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable memberId: String
    ): Unit

    /**
     * 스터디그룹에서 탈퇴시키고 블랙리스트로 등록합니다.
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param request 블랙리스트 등록 요청 정보
     */
    @Operation(summary = "스터디 그룹 멤버 블랙리스트 등록 API", description = "스터디 그룹에서 특정 멤버를 블랙리스트로 등록합니다. 재가입이 불가능합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 멤버 블랙리스트 등록 성공")
    @DeleteMapping("/{groupId}/blacklists")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer-jwt")
    fun addStudyGroupMemberToBlacklist(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid request: BlackListEnlistRequest
    ): Unit

    /**
     * 스터디 그룹의 블랙리스트 된 회원 목록을 조회합니다.
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     */
    @Operation(
        summary = "스터디 그룹 블랙리스트 조회 API",
        description = "스터디 그룹의 블랙리스트 된 회원 목록을 조회합니다."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "블랙리스트 조회 성공"),
    ])
    @GetMapping("/{groupId}/blacklists")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun getStudyGroupBlacklists(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String
    ): ListResponse<BlackListResponse>

    /**
     * 스터디 그룹에 추가된 블랙리스트 회원을 삭제합니다.
     */
    @Operation(
        summary = "스터디 그룹 블랙리스트 회원 삭제 API",
        description = "스터디 그룹의 블랙리스트 된 회원을 삭제합니다."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "블랙리스트 회원 삭제 성공")
    ])
    @DeleteMapping("/{groupId}/blacklists/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer-jwt")
    fun removeStudyGroupMemberFromBlacklist(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @PathVariable memberId: String
    ): Unit

    @Operation(
        summary = "스터디 그룹 정보 수정 API",
        description = "스터디 그룹의 이름과 설명을 수정합니다."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "스터디 그룹 정보 수정 성공"),
    ])
    @PutMapping("/{groupId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateStudyGroup(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid request: StudyGroupModifyRequest)
    : StudyGroupResponse

    /**
     * 스터디 그룹 소유자를 변경한다.
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param request 스터디 그룹 소유자 변경 요청 정보
     */
    @PatchMapping("/{groupId}/owner")
    @Operation(
        summary = "스터디 그룹 소유자 변경 API",
        description = "스터디 그룹의 소유자를 변경합니다."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "스터디 그룹 소유자 변경 성공"),
    ])
    @SecurityRequirement(name = "bearer-jwt")
    fun changeStudyGroupOwner(
        @LoginMember memberInfo: MemberInfo,
        @PathVariable groupId: String,
        @RequestBody @Valid request: ChangeStudyGroupOwnerRequest
    )


   /**
    * 스터디 그룹 멤버 목록 조회 API
    * @param memberInfo 로그인한 멤버의 정보
    * @param groupId 스터디 그룹 ID
    * @return 스터디 그룹 멤버 목록
    */
   @GetMapping("/{groupId}/group-members")
   @ResponseStatus(HttpStatus.OK)
   @SecurityRequirement(name = "bearer-jwt")
   @Operation(summary = "스터디 그룹 멤버 목록 조회 API", description = "스터디 그룹의 멤버 목록을 조회합니다.")
   @ApiResponses(value = [
       ApiResponse(responseCode = "200", description = "멤버 목록 조회 성공")
   ])
   fun getStudyGroupMembers(
       @LoginMember memberInfo: MemberInfo,
       @PathVariable groupId: String
   ): ListResponse<StudyGroupMemberResponse>
}
