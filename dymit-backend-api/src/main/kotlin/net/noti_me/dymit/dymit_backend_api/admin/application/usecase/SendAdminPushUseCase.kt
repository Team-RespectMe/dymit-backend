package net.noti_me.dymit.dymit_backend_api.admin.application.usecase

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`in`.web.dto.SendAdminPushCommand

/**
 * 관리자 푸시 알림을 전송하는 유스케이스입니다.
 */
interface SendAdminPushUseCase {

    /**
     * 관리자 요청에 포함된 모든 회원에게 푸시 알림을 전송합니다.
     *
     * @param command 전송 명령
     */
    fun execute(command: SendAdminPushCommand)
}
