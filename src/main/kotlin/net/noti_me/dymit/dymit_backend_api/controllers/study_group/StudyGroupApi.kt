package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.response.ListResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.*

@Tag(name = "스터디 그룹 API", description = "스터디 그룹 관련 API")
interface StudyGroupApi {

    /**
     * 스터디 그룹 생성 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param request 스터디 그룹 생성 요청 정보
     * @return 생성된 스터디 그룹의 정보
     */
    @Operation(summary = "스터디 그룹 생성 API", description = "로그인한 멤버가 새로운 스터디 그룹을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "스터디 그룹 생성 성공")
    @SecurityRequirement(name="bearer-jwt")
    fun createStudyGroup(
        memberInfo: MemberInfo,
        request: StudyGroupCreateRequest
    ): StudyGroupResponse

    /**
     * 스터디 그룹 가입 요청
     */
    @Operation(summary = "스터디 그룹 가입 API", description = "스터디 그룹에 가입합니다.")
    @ApiResponse(responseCode = "201", description = "가입 성공")
    @SecurityRequirements(SecurityRequirement(name = "bearer-jwt"))
    fun joinStudyGroup(
        memberInfo: MemberInfo,
        groupId: String,
        request: StudyGroupJoinRequest
    ): StudyGroupMemberResponse

    /**
     * 스터디 그룹 검색 API
     */
    @Operation(summary = "초대 코드를 통한 스터디 그룹 검색 API", description = "초대 코드에 따른 스터디 그룹을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @SecurityRequirements(SecurityRequirement(name = "bearer-jwt"))
    fun searchStudyGroupByInviteCode(
        memberInfo: MemberInfo,
         inviteCode: String
    ): StudyGroupResponse

    /**
     * 로그인 한 사용자가 속한 스터디 그룹 목록을 반환합니다.
     */
    @Operation(summary = "내 스터디 그룹 목록 조회 API", description = "로그인한 사용자가 속한 스터디 그룹 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun getMyStudyGroups(
        @LoginMember memberInfo: MemberInfo
    ): ListResponse<StudyGroupListItemDto>

    /**
     * 스터디 그룹의 Invite Code를 조회합니다.
     */
    @Operation(summary = "스터디 그룹 Invite Code 조회 API", description = "스터디 그룹의 Invite Code를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun getStudyGroupInviteCode(
        memberInfo: MemberInfo,
        groupId: String
    ): InviteCodeResponse

    /**
     * 스터디 그룹 세부 조회 API
     */
    @Operation(summary = "스터디 그룹 세부 조회 API", description = "스터디 그룹의 세부 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearer-jwt")

    fun getStudyGroup(
        memberInfo: MemberInfo,
        groupId: String
    ): StudyGroupQueryDetailResponse

    /**
     * 스터디 그룹 프로필 이미지 업데이트 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param StudyGroupImageUpdateRequest 스터디 그룹 이미지 업데이트 요청 정보
     */
    @Operation(summary = "스터디 그룹 프로필 이미지 업데이트 API", description = "스터디 그룹의 프로필 이미지를 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 이미지 업데이트 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun updateStudyGroupProfileImage(
        memberInfo: MemberInfo,
        groupId: String,
        request: UpdateStudyGroupProfileImageRequest
    ): StudyGroupResponse

    /**
     * 스터디 그룹 삭제 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     */
    @Operation(summary = "스터디 그룹 삭제 API", description = "스터디 그룹을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 삭제 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun deleteStudyGroup(
        memberInfo: MemberInfo,
        groupId: String
    )

    /**
     * 스터디 그룹 탈퇴 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     */
    @Operation(summary = "스터디 그룹 탈퇴 API", description = "스터디 그룹에서 탈퇴합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 탈퇴 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun leaveStudyGroup(
        memberInfo: MemberInfo,
        groupId: String
    )

    /**
     * 스터디 그룹 멤버 퇴장 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param memberId 강퇴할 멤버 ID
     */
    @Operation(summary = "스터디 그룹 멤버 삭제 API", description = "스터디 그룹에서 탈퇴할 때 사용합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 멤버 강퇴 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun removeStudyGroupMember(
        memberInfo: MemberInfo,
        groupId: String,
        memberId: String
    )

    /**
     * 스터디그룹에서 탈퇴시키고 블랙리스트로 등록합니다.
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param request 블랙리스트 등록 요청 정보
     */
    @Operation(summary = "스터디 그룹 멤버 블랙리스트 등록 API", description = "스터디 그룹에서 특정 멤버를 블랙리스트로 등록합니다. 재가입이 불가능합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 그룹 멤버 블랙리스트 등록 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun addStudyGroupMemberToBlacklist(
        memberInfo: MemberInfo,
        groupId: String,
        request: BlackListEnlistRequest
    )

    /**
     * 스터디 그룹의 블랙리스트 된 회원 목록을 조회합니다.
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     */
    @Operation(summary = "스터디 그룹 블랙리스트 조회 API", description = "스터디 그룹의 블랙리스트 된 회원 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "블랙리스트 조회 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun getStudyGroupBlacklists(
        memberInfo: MemberInfo,
        groupId: String
    ): ListResponse<BlackListResponse>

    /**
     * 스터디 그룹에 추가된 블랙리스트 회원을 삭제합니다.
     */
    @Operation(summary = "스터디 그룹 블랙리스트 회원 삭제 API", description = "스터디 그룹의 블랙리스트 된 회원을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "블랙리스트 회원 삭제 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun removeStudyGroupMemberFromBlacklist(
        memberInfo: MemberInfo,
        groupId: String,
        memberId: String
    )

    @Operation(summary = "스터디 그룹 정보 수정 API", description = "스터디 그룹의 이름과 설명을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "스터디 그룹 정보 수정 성공")
    @SecurityRequirement(name="bearer-jwt")
    fun updateStudyGroup(
        memberInfo: MemberInfo,
        groupId: String,
        request: StudyGroupModifyRequest
    ): StudyGroupResponse

    /**
     * 스터디 그룹 소유자를 변경한다.
     * @param memberInfo 로그인한 멤버의 정보
     * @param groupId 스터디 그룹 ID
     * @param request 스터디 그룹 소유자 변경 요청 정보
     */
    @Operation(summary = "스터디 그룹 소유자 변경 API", description = "스터디 그룹의 소유자를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "스터디 그룹 소유자 변경 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun changeStudyGroupOwner(
        memberInfo: MemberInfo,
        groupId: String,
        request: ChangeStudyGroupOwnerRequest
    )


   /**
    * 스터디 그룹 멤버 목록 조회 API
    * @param memberInfo 로그인한 멤버의 정보
    * @param groupId 스터디 그룹 ID
    * @return 스터디 그룹 멤버 목록
    */
    @Operation(summary = "스터디 그룹 멤버 목록 조회 API", description = "스터디 그룹의 멤버 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "멤버 목록 조회 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun getStudyGroupMembers(
        memberInfo: MemberInfo,
        groupId: String
    ): ListResponse<StudyGroupMemberResponse>
}
