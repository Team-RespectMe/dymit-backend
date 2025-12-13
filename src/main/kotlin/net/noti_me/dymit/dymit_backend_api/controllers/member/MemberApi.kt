package net.noti_me.dymit.dymit_backend_api.controllers.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.parameters.ValidatedParameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.*

@Tag(name = "멤버 API", description = "멤버 관련 API")
interface MemberApi {

    @Operation(
        method = "GET",
        summary = "멤버 프로필 조회",
        description = "특정 멤버의 프로필 정보를 조회합니다. 자신이 아닌 다른 멤버의 프로필을 조회할 경우, 접근 권한이 없을 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "멤버 조회 성공",)
    @SecurityRequirement(name = "bearer-jwt")
    fun getMemberProfile(loginMember: MemberInfo, memberId: String): MemberProfileResponse

    @Operation(
        method = "PATCH",
        summary = "멤버 닉네임 업데이트",
        description = "특정 멤버의 닉네임을 업데이트합니다. 자신이 아닌 멤버의 닉네임을 업데이트하려고 할 경우, 접근 권한이 없을 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "멤버 프로필 업데이트 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun patchNickname(
        loginMember: MemberInfo,
        memberId: String,
        @Valid request: MemberNicknameUpdateRequest
    ): MemberProfileResponse

    @Operation(method = "POST", summary = "멤버 생성", description = """새로운 멤버를 생성합니다.""")
    @ApiResponse(responseCode = "201", description = "멤버 생성 성공")
    fun createMember(@Valid request: MemberCreateRequest): MemberCreateResponse

    @Operation(method = "GET", summary = "닉네임 유효성 검사", description = "닉네임 중복체크 및 유효성 검사를 수행합니다.")
    @ApiResponse(responseCode = "200", description = "닉네임이 유효합니다.")
    @Parameter(name = "nickname", description = "검증할 닉네임", required = true, `in` = ParameterIn.QUERY)
    @SecurityRequirement(name = "bearer-jwt")
    fun checkNickname( nickname: String): Unit

    @Operation(
        method = "PUT",
        summary = "멤버 프로필 이미지 업로드",
        description = "멤버의 프로필 이미지를 업로드합니다. 이미지를 업로드하면 해당 멤버의 프로필 이미지가 업데이트됩니다. 프리셋 사용 시 type 이 반드시 presets 로 명시되어야하며, presetNo 필드가 주어져야합니다."
    )
    @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공",)
    @SecurityRequirement(name = "bearer-jwt")
    fun uploadProfileImage(
        loginMember: MemberInfo,
        memberId: String,
        @Valid request: ProfileImageUploadRequest
    ): MemberProfileResponse

    @Operation(method = "DELETE", summary = "회원 탈퇴 삭제 API", description = "회원을 삭제합니다, 단 소프트 삭제이기 때문에 OIDC 연동 정보만 제거 됩니다.")
    @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun deleteMember(
        loginMember: MemberInfo,
        memberId: String
    ): Unit

    @Operation(method = "POST", summary = "디바이스 토큰 등록 API", description = "디바이스 토큰을 등록합니다.")
    @ApiResponse(responseCode = "201", description = "디바이스 토큰 등록 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun registerDeviceToken(
        loginMember: MemberInfo,
        memberId: String,
        @Valid request: DeviceTokenCommandRequest
    )

    @Operation(method = "DELETE", summary = "디바이스 토큰 삭제 API", description = "디바이스 토큰을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "디바이스 토큰 삭제 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun unregisterDeviceToken(
        loginMember: MemberInfo,
        memberId: String,
        @Valid request: DeviceTokenCommandRequest
    ): Unit

    @Operation(
        method = "PATCH",
        summary = "멤버 관심사 업데이트",
        description = "특정 멤버의 관심사를 업데이트합니다. 자신이 아닌 멤버의 관심사를 업데이트하려고 할 경우, 접근 권한이 없을 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "멤버 관심사 업데이트 성공")
    @SecurityRequirement(name = "bearer-jwt")
    fun patchInterests(
        loginMember: MemberInfo,
        memberId: String,
        @Valid request: UpdateInterestsRequest
    ): MemberProfileResponse
}
