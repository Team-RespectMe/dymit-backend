package net.noti_me.dymit.dymit_backend_api.admin.adapter.`in`.web

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.AdminPushNotificationApi
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.AdminPushNotificationRequest
import net.noti_me.dymit.dymit_backend_api.admin.application.usecase.SendAdminPushUseCase
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 관리자 푸시 알림 REST 요청을 처리하는 입력 어댑터입니다.
 */
@RestController
class AdminPushNotificationController(
    private val sendAdminPushUseCase: SendAdminPushUseCase
) : AdminPushNotificationApi {

    /**
     * 기존 관리자 푸시 알림 엔드포인트를 처리합니다.
     */
    @PostMapping("/api/v1/admin/push-notifications")
    @ResponseStatus(HttpStatus.CREATED)
    override fun sendPushNotifications(
        @LoginMember admin: MemberInfo,
        @RequestBody request: AdminPushNotificationRequest
    ) {
        sendAdminPushUseCase.execute(request.toCommand())
    }
}
