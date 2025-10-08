package net.noti_me.dymit.dymit_backend_api.application.push_notification.impl

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import net.noti_me.dymit.dymit_backend_api.application.push_notification.PushService
import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FirebasePushService(
    private val app: FirebaseApp,
    private val loadMemberPort: LoadMemberPort,
    private val groupMemberRepository: StudyGroupMemberRepository
) : PushService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun sendPersonalPush(message: PersonalPushMessage) {
        val member = loadMemberPort.loadById(message.memberId)
            ?: return

        val deviceTokens = member.deviceTokens
            .filter { it.isActive }
            .map(DeviceToken::token)

        return sendPushNotifications(
            deviceTokens = deviceTokens,
            title = message.title,
            body = message.body,
            image = message.image,
            data = message.data
        )
    }

    override fun sendGroupPush(message: GroupPushMessage) {
        val groupMembers = groupMemberRepository.findByGroupId(message.groupId)
        val members = loadMemberPort.loadByIds(groupMembers.map { it.memberId.toHexString() })
        val deviceTokens = members.flatMap { member ->
            member.deviceTokens
                .filter { it.isActive }
                .map(DeviceToken::token)
        }.distinct()

        return sendPushNotifications(
            deviceTokens = deviceTokens,
            title = message.title,
            body = message.body,
            image = message.image,
            data = message.data
        )
    }
    /**
     * 여러 사용자에게 Multicast 방식으로 푸시 알림 전송
     * @param deviceTokens 푸시 알림을 받을 디바이스 토큰 리스트
     * @param title 푸시 알림 제목
     * @param body 푸시 알림 내용
     * @param image 푸시 알림 이미지 URL (선택 사항)
     * @param data 추가 데이터 (선택 사항)
     */
    private fun sendPushNotifications(
        deviceTokens: List<String>,
        title: String,
        body: String,
        image: String?,
        data: Map<String, String>
    ) {
        if (deviceTokens.isEmpty()) {
            logger.info("No active device tokens to send push notification.")
            return
        }

        // multicast
        val notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .apply {
                if (image != null) {
                    setImage(image)
                }
            }
            .build()
        val messageBuilder = com.google.firebase.messaging.MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(notification)
        data.forEach { (key, value) ->
            messageBuilder.putData(key, value)
        }
        val message = messageBuilder.build()
        try {
            val response = FirebaseMessaging.getInstance(app).sendMulticast(message)
            if (response.failureCount > 0) {
                response.responses.forEachIndexed { index, sendResponse ->
                    if (!sendResponse.isSuccessful) {
                        logger.error("Failed to send message to ${deviceTokens[index]}: ${sendResponse.exception}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending multicast message", e)
        }
    }
}