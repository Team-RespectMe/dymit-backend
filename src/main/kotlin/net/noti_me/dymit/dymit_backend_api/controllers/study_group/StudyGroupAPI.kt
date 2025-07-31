package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupJoinRequest
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupListItemDto
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupMemberResponse
import net.noti_me.dymit.dymit_backend_api.controllers.study_group.dto.StudyGroupResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "스터디 그룹 API", description = "스터디 그룹 관련 API")
@RequestMapping("/api/v1/study-groups")
interface StudyGroupAPI {

    /**
     * 스터디 그룹 생성 API
     * @param memberInfo 로그인한 멤버의 정보
     * @param request 스터디 그룹 생성 요청 정보
     * @return 생성된 스터디 그룹의 정보
     */
    @Operation(summary = "스터디 그룹 생성 API", description = "로그인한 멤버가 새로운 스터디 그룹을 생성합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "스터디 그룹 생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청 (입력 값이 유효하지 않을 때)"),
        ApiResponse(responseCode = "401", description = "인증 실패 (로그인하지 않았거나 토큰이 유효하지 않을 때)"),
        ApiResponse(responseCode = "403", description = "접근 권한 없음 (로그인한 멤버가 스터디 그룹을 생성할 수 없는 경우)"),
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
        ApiResponse(responseCode = "400", description = "잘못된 요청 (검색어가 유효하지 않을 때)"),
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
        ApiResponse(responseCode = "401", description = "인증 실패 (로그인하지 않았거나 토큰이 유효하지 않을 때)"),
        ApiResponse(responseCode = "404", description = "사용자가 없는 경우")
    ])
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun getMyStudyGroups(
        @LoginMember memberInfo: MemberInfo
    ): List<StudyGroupListItemDto>
}