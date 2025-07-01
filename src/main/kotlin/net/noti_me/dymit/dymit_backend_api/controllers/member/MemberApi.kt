package net.noti_me.dymit.dymit_backend_api.controllers.member

import io.swagger.v3.oas.annotations.responses.*
import org.springframework.web.bind.annotation.*
import net.noti_me.dymit.dymit_backend_api.controllers.member.dto.*

interface MemberApi {

    @ApiResponses( value = [
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
    ])
    fun getMemberProfile(@PathVariable memberId: String): MemberProfileResponse


    @ApiResponses( value = [
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
    ])
    @PatchMapping("/{memberId}/nickname")
    fun patchNickname(@PathVariable memberId: String, 
        @RequestBody request: MemberNicknameUpdateRequest
    ): MemberProfileResponse
}