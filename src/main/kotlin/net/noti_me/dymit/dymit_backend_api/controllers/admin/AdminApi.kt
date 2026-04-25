package net.noti_me.dymit.dymit_backend_api.controllers.admin

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.admin.dto.AdminPushNotificationRequest

@Tag(name = "Admin API", description = "관리자 API")
interface AdminApi {

    @Operation(
        method = "POST",
        summary = "푸시 알림 전송",
        description = "관리자가 특정 사용자들에게 푸시 알림을 전송할 수 있는 API입니다."
    )
    @ApiResponse(
        responseCode = "201",
        description = "푸시 알림이 성공적으로 전송되었습니다."
    )
    fun sendPushNotifications(
        admin: MemberInfo,
        request: AdminPushNotificationRequest
    )
}