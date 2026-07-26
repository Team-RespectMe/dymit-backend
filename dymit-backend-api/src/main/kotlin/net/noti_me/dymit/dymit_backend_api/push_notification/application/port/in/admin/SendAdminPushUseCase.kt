package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.admin

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.admin.dto.SendAdminPushCommand

/**
 * 관리자 푸시 알림 전송 유스케이스입니다.
 */
interface SendAdminPushUseCase {

    /**
     * 관리자 요청에 포함된 모든 회원에게 푸시 알림을 전송합니다.
     */
    fun execute(command: SendAdminPushCommand)
}
