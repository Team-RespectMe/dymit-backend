package net.noti_me.dymit.dymit_backend_api.controllers.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.application.auth.dto.LoginResult
import org.springframework.web.bind.annotation.RequestBody
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcLoginRequest
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.RefreshTokenSubmitRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus


@RequestMapping("/api/v1/")
@Tag(name = "인증 API", description = "인증 관련 API")
interface AuthApi {

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "OIDC 로그인 성공, 토큰 발급"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (예: ID 토큰이 유효하지 않거나 누락된 경우)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (예: ID 토큰이 변조 되었거나 만료된 경우)"
            ),
        ]
    )
    @PostMapping("/auth/oidc")
    @ResponseStatus(HttpStatus.CREATED)
    fun oidcLogin(
        @RequestBody request: OidcLoginRequest
    ) : LoginResult

    @Operation(
        summary = "액세스 토큰 재발급",
        description = """리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. 리프레시 토큰이 유효하지 않거나 만료된 경우, 인증 실패가 발생합니다.
            <br>
            발급 시 요청 시 사용한 리프레시 토큰도 같이 발급됩니다. *이 때 리프레시 토큰의 유효기간이 얼마 남지 않은 경우 자동으로 새로운 리프레시 토큰을 재발급합니다.*
            따라서 반드시 응답에 포함된 리프레시 토큰을 클라이언트에서 저장해야 합니다.
        """

    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "액세스 토큰 재발급 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (예: 리프레시 토큰이 유효하지 않거나 누락된 경우)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (예: 리프레시 토큰이 만료된 경우)"
            ),
        ]
    )
    @PostMapping("/auth/jwt/reissue")
    @ResponseStatus(HttpStatus.OK)
    fun reissueAccessToken(
        @RequestBody @Valid request: RefreshTokenSubmitRequest
    ): LoginResult

    @Operation(
        summary = "JWT 블랙리스트 등록",
        description = "특정 JWT를 블랙리스트에 등록하여 더 이상 사용되지 않도록 합니다. 주로 로그아웃 시 사용됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "블랙리스트 등록 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (예: JWT가 유효하지 않거나 누락된 경우)"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (예: JWT가 만료된 경우)"
            ),
        ]
    )
    @PostMapping("/auth/jwt/blacklists")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestBody @Valid request: RefreshTokenSubmitRequest)
}