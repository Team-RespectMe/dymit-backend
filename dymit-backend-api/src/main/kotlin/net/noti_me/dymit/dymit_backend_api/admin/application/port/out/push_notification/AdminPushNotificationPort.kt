package net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.dto.AdminPushNotificationDto

/**
 * 관리자 모듈이 개인 푸시 알림을 전달하는 출력 포트입니다.
 */
interface AdminPushNotificationPort {

    /**
     * 개인 푸시 알림을 전달합니다.
     *
     * @param notification 관리자 소유 푸시 DTO
     */
    fun send(notification: AdminPushNotificationDto)
}
