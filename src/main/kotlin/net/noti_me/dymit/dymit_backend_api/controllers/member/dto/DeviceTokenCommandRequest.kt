package net.noti_me.dymit.dymit_backend_api.controllers.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.noti_me.dymit.dymit_backend_api.common.annotation.Sanitize

/**
 * 디바이스 토큰 등록/해제 요청 DTO
 * @param deviceToken 디바이스 토큰 문자열
 */
@Sanitize
@Schema(description = "디바이스 토큰 등록/해제 요청 DTO")
class DeviceTokenCommandRequest(
    @field:Schema(description = "디바이스 토큰 문자열", example = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890")
    val deviceToken: String
) {
}