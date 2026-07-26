package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.personal.dto.SendPersonalPushCommand

/**
 * 개인 푸시 알림 전송 유스케이스입니다.
 */
interface SendPersonalPushUseCase {

    /**
     * 지정한 회원에게 푸시 알림을 전송합니다.
     */
    fun execute(command: SendPersonalPushCommand)
}
