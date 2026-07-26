package net.noti_me.dymit.dymit_backend_api.admin.adapter.`out`.push_notification

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.AdminPushNotificationPort
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.dto.AdminPushNotificationDto
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushEventData
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushMessagesEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * 관리자 푸시 출력 포트를 모듈 독립 애플리케이션 이벤트에 연결합니다.
 */
@Component
class AdminPushNotificationAdapter(
    private val eventPublisher: ApplicationEventPublisher
) : AdminPushNotificationPort {

    /**
     * 관리자 소유 푸시 DTO를 모듈 독립 이벤트로 발행합니다.
     */
    override fun send(notification: AdminPushNotificationDto) {
        eventPublisher.publishEvent(
            AdminPersonalPushEvent(
                message = PersonalPushEventData(
                    memberId = notification.memberId,
                    eventName = notification.eventName,
                    title = notification.title,
                    body = notification.body,
                    image = notification.image,
                    data = notification.data
                )
            )
        )
    }

    /**
     * 관리자 출력 어댑터가 발행하는 단일 개인 푸시 이벤트입니다.
     */
    private data class AdminPersonalPushEvent(
        val message: PersonalPushEventData
    ) : PersonalPushMessagesEvent {

        /**
         * 개인 푸시 메시지를 반환합니다.
         */
        override fun toPersonalPushMessages(): List<PersonalPushEventData> {
            return listOf(message)
        }
    }
}
