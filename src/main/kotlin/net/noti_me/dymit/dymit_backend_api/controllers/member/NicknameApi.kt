package net.noti_me.dymit.dymit_backend_api.controllers.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.noti_me.dymit.dymit_backend_api.common.constraints.nickname.Nickname
import net.noti_me.dymit.dymit_backend_api.common.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/members")
@Tag(name = "닉네임 API", description = "닉네임 관련 API")
interface NicknameApi {

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
    fun checkNickname(@RequestParam @Valid @Nickname nickname: String): Unit
}