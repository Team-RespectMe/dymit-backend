package net.noti_me.dymit.dymit_backend_api.push_notification.adapter.out.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.SendPushNotificationPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.delivery.dto.PushDeliveryDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Firebase multicast API로 푸시 메시지를 전송하는 출력 어댑터입니다.
 */
@Component
class FirebasePushNotificationAdapter(
    private val app: FirebaseApp
) : SendPushNotificationPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 푸시 전송 DTO를 Firebase multicast 메시지로 변환해 전송합니다.
     */
    override fun send(message: PushDeliveryDto) {
        if (message.deviceTokens.isEmpty()) {
            logger.info("No active device tokens to send push notification.")
            return
        }

        val notification = Notification.builder()
            .setTitle(message.title)
            .setBody(message.body)
            .apply {
                if (message.image != null) {
                    setImage(message.image)
                }
            }
            .build()
        val messageBuilder = MulticastMessage.builder()
            .addAllTokens(message.deviceTokens)
            .setNotification(notification)
        message.data.forEach { (key, value) ->
            messageBuilder.putData(key, value)
        }

        try {
            val response = FirebaseMessaging.getInstance(app)
                .sendEachForMulticast(messageBuilder.build())
            if (response.failureCount > 0) {
                response.responses.forEachIndexed { index, sendResponse ->
                    if (!sendResponse.isSuccessful) {
                        logger.error(
                            "Failed to send message to ${message.deviceTokens[index]}: ${sendResponse.exception}"
                        )
                    }
                }
            }
        } catch (exception: Exception) {
            logger.error("Error sending multicast message", exception)
        }
    }
}
