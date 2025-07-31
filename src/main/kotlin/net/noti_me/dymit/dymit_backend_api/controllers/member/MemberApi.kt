package net.noti_me.dymit.dymit_backend_api.controllers.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import net.noti_me.dymit.dymit_backend_api.common.response.ErrorResponse
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberNicknameUpdateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberProfileResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.ProfileImageUploadRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "멤버 API", description = "멤버 관련 API")
@RequestMapping("/api/v1/members")
interface MemberApi {

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
        summary = "멤버 프로필 조회",
        description = "특정 멤버의 프로필 정보를 조회합니다. 자신이 아닌 다른 멤버의 프로필을 조회할 경우, 접근 권한이 없을 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "멤버 조회 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "잘못된 요청(멤버 ID가 유효하지 않을 때)",
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음(자신이 아닌 멤버의 프로필을 조회하려고 할 때)",
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
            ),
            ApiResponse(
                responseCode = "404",
                description = "멤버를 찾을 수 없음",
            ),
        ]
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{memberId}")
    fun getMemberProfile(@LoginMember loginMember: MemberInfo, @PathVariable memberId: String): MemberProfileResponse


    @Operation(
        summary = "멤버 닉네임 업데이트",
        description = "특정 멤버의 닉네임을 업데이트합니다. 자신이 아닌 멤버의 닉네임을 업데이트하려고 할 경우, 접근 권한이 없을 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "멤버 프로필 업데이트 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청(유효하지 않은 데이터 형식)",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패",
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음(자신이 아닌 멤버의 프로필을 업데이트하려고 할 때)",
            ),
            ApiResponse(
                responseCode = "404",
                description = "멤버를 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
            ),
        ]
    )
    @PatchMapping("/{memberId}/nickname")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun patchNickname(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @RequestBody @Valid request: MemberNicknameUpdateRequest
    ): MemberProfileResponse

    @Operation(
        summary = "멤버 생성",
        description = """새로운 멤버를 생성합니다."""
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "멤버 생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청(유효하지 않은 데이터 형식)",
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
            ),
        ]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createMember(@RequestBody @Valid request: MemberCreateRequest): MemberCreateResponse

    @Operation(summary = "닉네임 유효성 검사", description = "닉네임 중복체크 및 유효성 검사를 수행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "닉네임이 유효합니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청(유효하지 않은 닉네임 형식)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "닉네임이 이미 사용 중입니다.",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/nickname-validation")
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "nickname", description = "검증할 닉네임", required = true, `in` = ParameterIn.QUERY)
    @SecurityRequirement(name = "bearer-jwt")
    fun checkNickname(@RequestParam @Valid @Nickname nickname: String): Unit

    @Operation(
        summary = "멤버 프로필 이미지 업로드",
        description = "멤버의 프로필 이미지를 업로드합니다. 이미지를 업로드하면 해당 멤버의 프로필 이미지가 업데이트됩니다. 프리셋 사용 시 type 이 반드시 presets 로 명시되어야하며, presetNo 필드가 주어져야합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "프로필 이미지 업로드 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청(유효하지 않은 데이터 형식)",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패",
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음(자신이 아닌 멤버의 프로필 이미지를 업로드하려고 할 때)",
            ),
            ApiResponse(
                responseCode = "404",
                description = "멤버를 찾을 수 없음",
            )
        ]
    )
    @PutMapping("/{memberId}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearer-jwt")
    fun uploadProfileImage(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String,
        @ModelAttribute @Valid request: ProfileImageUploadRequest
    ): MemberProfileResponse

    @Operation(summary = "회원 탈퇴 삭제 API", description = "회원을 삭제합니다, 단 소프트 삭제이기 때문에 OIDC 연동 정보만 제거 됩니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
    ])
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearer-jwt")
    fun deleteMember(
        @LoginMember loginMember: MemberInfo,
        @PathVariable memberId: String
    ): Unit

}
