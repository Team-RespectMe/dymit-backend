package net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.auth.application.port.`in`.server_to_server.dto.AppleRequest

/**
 * 외부 인증 제공자의 서버 간 이벤트 수신 API입니다.
 */
interface ServerToServerApi {

    /**
     * Apple 서버 간 이벤트를 처리합니다.
     *
     * @param request Apple 이벤트 요청
     */
    fun appleS2SEndpoint(request: AppleRequest)
}
