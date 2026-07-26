package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.`in`.group.dto.SendGroupPushCommand

/**
 * 그룹 푸시 알림 전송 유스케이스입니다.
 */
interface SendGroupPushUseCase {

    /**
     * 지정한 그룹의 회원들에게 푸시 알림을 전송합니다.
     */
    fun execute(command: SendGroupPushCommand)
}
