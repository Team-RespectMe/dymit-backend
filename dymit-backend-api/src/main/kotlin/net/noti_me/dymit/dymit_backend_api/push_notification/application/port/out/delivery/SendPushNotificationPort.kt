package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.dto.PushDeliveryDto

/**
 * 외부 푸시 공급자로 메시지를 전송하는 출력 포트입니다.
 */
interface SendPushNotificationPort {

    /**
     * 멀티캐스트 푸시 메시지를 전송합니다.
     */
    fun send(message: PushDeliveryDto)
}
