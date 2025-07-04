package net.noti_me.dymit.dymit_backend_api.controllers.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberCreateResponse
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberNicknameUpdateRequest
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.MemberProfileResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "멤버 API", description = "멤버 관련 API")
@RequestMapping("/api/v1/members")
interface MemberApi {

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
}
