package net.noti_me.dymit.dymit_backend_api.controllers.admin

import net.noti_me.dymit.dymit_backend_api.application.admin.AdminServiceFacade
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.admin.dto.AdminPushNotificationRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RestController
class AdminApiController(
    private val adminServiceFacade: AdminServiceFacade
): AdminApi {

    @PostMapping("/api/v1/admin/push-notifications")
    @ResponseStatus(HttpStatus.CREATED)
    override fun sendPushNotifications(
        @LoginMember admin: MemberInfo,
        @RequestBody request: AdminPushNotificationRequest
    ) {
        adminServiceFacade.sendPushNotifications(admin, request.toCommand())
    }
}