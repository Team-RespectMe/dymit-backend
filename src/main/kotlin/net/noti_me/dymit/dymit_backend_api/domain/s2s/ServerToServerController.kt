package net.noti_me.dymit.dymit_backend_api.domain.s2s

import net.noti_me.dymit.dymit_backend_api.application.auth.server_to_server.AppleS2SAlarmService
import net.noti_me.dymit.dymit_backend_api.domain.s2s.dto.AppleRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.PermitAll

@RestController
@RequestMapping("/api/v1/s2s/")
class ServerToServerController(
    private val appleS2SAlarmService: AppleS2SAlarmService
) {

    @PostMapping("/apple")
    @PermitAll
    fun appleS2SEndpoint( @RequestBody request: AppleRequest  ) {
        appleS2SAlarmService.handleEvent(request.toCommand())
    }
}