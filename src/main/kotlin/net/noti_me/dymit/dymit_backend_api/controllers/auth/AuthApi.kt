package net.noti_me.dymit.dymit_backend_api.controllers.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.LoginResponse
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.OidcLoginRequest
import net.noti_me.dymit.dymit_backend_api.controllers.auth.dto.RefreshTokenSubmitRequest
import org.springframework.web.bind.annotation.RequestBody


@Tag(name = "인증 API", description = "인증 관련 API")
interface AuthApi {

    @ApiResponse(responseCode = "201", description = "OIDC 로그인 성공, 토큰 발급")
    @Operation(method = "POST",
        summary = "OIDC 로그인",
        description = "OIDC 제공자의 ID 토큰을 사용하여 사용자를 인증하고, JWT 액세스 토큰과 리프레시 토큰을 발급합니다."
    )
    fun oidcLogin(request: OidcLoginRequest) : LoginResponse

    @Operation(
        method = "POST",
        summary = "액세스 토큰 재발급",
        description = """리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. 리프레시 토큰이 유효하지 않거나 만료된 경우, 인증 실패가 발생합니다.
            <br>
            발급 시 요청 시 사용한 리프레시 토큰도 같이 발급됩니다. *이 때 리프레시 토큰의 유효기간이 얼마 남지 않은 경우 자동으로 새로운 리프레시 토큰을 재발급합니다.*
            따라서 반드시 응답에 포함된 리프레시 토큰을 클라이언트에서 저장해야 합니다.
        """
    )
    @ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공")
    fun reissueAccessToken(request: RefreshTokenSubmitRequest): LoginResponse

    @Operation(
        method = "POST",
        summary = "JWT 블랙리스트 등록",
        description = "특정 JWT를 블랙리스트에 등록하여 더 이상 사용되지 않도록 합니다. 주로 로그아웃 시 사용됩니다."
    )
    @ApiResponse(responseCode = "204", description = "블랙리스트 등록 성공")
    fun logout(@RequestBody @Valid request: RefreshTokenSubmitRequest)
}