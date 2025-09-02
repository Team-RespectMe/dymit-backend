package net.noti_me.dymit.dymit_backend_api.units.application.push_notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.SendResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.push_notification.impl.FirebasePushService

/**
 * FirebasePushService 클래스의 푸시 알림 전송 기능을 테스트한다.
 */
class FirebasePushServiceTest : BehaviorSpec({

    val firebaseApp = mockk<FirebaseApp>()
    val firebaseMessaging = mockk<FirebaseMessaging>()
    val firebasePushService = FirebasePushService(firebaseApp)

    beforeEach {
        // FirebaseMessaging 정적 메서드 모킹
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance(firebaseApp) } returns firebaseMessaging
    }

    given("FirebasePushService가 정상적으로 설정된 상황에서") {
        val deviceToken = "test-device-token"
        val title = "테스트 제목"
        val body = "테스트 내용"

        `when`("단일 디바이스에 기본 푸시 알림을 전송하면") {
            every { firebaseMessaging.send(any()) } returns "message-id-123"

            then("Firebase를 통해 메시지가 성공적으로 전송되어야 한다") {
                firebasePushService.sendPushNotification(
                    deviceToken = deviceToken,
                    title = title,
                    body = body,
                    image = null,
                    data = emptyMap()
                )

                verify { firebaseMessaging.send(any()) }
            }
        }

        `when`("이미지와 데이터를 포함한 푸시 알림을 전송하면") {
            val imageUrl = "https://example.com/image.jpg"
            val additionalData = mapOf("key1" to "value1", "key2" to "value2")
            every { firebaseMessaging.send(any()) } returns "message-id-456"

            then("모든 정보가 포함된 메시지가 전송되어야 한다") {
                firebasePushService.sendPushNotification(
                    deviceToken = deviceToken,
                    title = title,
                    body = body,
                    image = imageUrl,
                    data = additionalData
                )

                verify { firebaseMessaging.send(any()) }
            }
        }
    }

    given("Firebase에서 오류가 발생하는 상황에서") {
        val deviceToken = "test-device-token"
        val title = "테스트 제목"
        val body = "테스트 내용"

        `when`("단일 푸시 알림 전송 중 예외가 발생하면") {
            val exception = RuntimeException("Firebase error")
            every { firebaseMessaging.send(any()) } throws exception

            then("예외를 안전하게 처리하고 메서드가 정상 완료되어야 한다") {
                firebasePushService.sendPushNotification(
                    deviceToken = deviceToken,
                    title = title,
                    body = body,
                    image = null,
                    data = emptyMap()
                )

                verify { firebaseMessaging.send(any()) }
            }
        }
    }

    given("멀티캐스트 푸시 알림 환경이 준비된 상황에서") {
        val deviceTokens = listOf("token1", "token2", "token3")
        val title = "멀티캐스트 제목"
        val body = "멀티캐스트 내용"

        `when`("여러 디바이스에 동시에 푸시 알림을 전송하면") {
            val successResponse1 = mockk<SendResponse> {
                every { isSuccessful } returns true
            }
            val successResponse2 = mockk<SendResponse> {
                every { isSuccessful } returns true
            }
            val successResponse3 = mockk<SendResponse> {
                every { isSuccessful } returns true
            }
            val batchResponse = mockk<BatchResponse> {
                every { failureCount } returns 0
                every { responses } returns listOf(successResponse1, successResponse2, successResponse3)
            }
            every { firebaseMessaging.sendMulticast(any()) } returns batchResponse

            then("모든 디바이스에 성공적으로 전송되어야 한다") {
                firebasePushService.sendPushNotifications(
                    deviceTokens = deviceTokens,
                    title = title,
                    body = body,
                    image = null,
                    data = emptyMap()
                )

                verify { firebaseMessaging.sendMulticast(any()) }
                batchResponse.failureCount shouldBe 0
            }
        }

        `when`("이미지와 데이터를 포함한 멀티캐스트를 전송하면") {
            val imageUrl = "https://example.com/multicast-image.jpg"
            val additionalData = mapOf("type" to "multicast", "category" to "notification")
            val successResponse = mockk<SendResponse> {
                every { isSuccessful } returns true
            }
            val batchResponse = mockk<BatchResponse> {
                every { failureCount } returns 0
                every { responses } returns listOf(successResponse, successResponse, successResponse)
            }
            every { firebaseMessaging.sendMulticast(any()) } returns batchResponse

            then("모든 정보가 포함된 멀티캐스트가 성공해야 한다") {
                firebasePushService.sendPushNotifications(
                    deviceTokens = deviceTokens,
                    title = title,
                    body = body,
                    image = imageUrl,
                    data = additionalData
                )

                verify { firebaseMessaging.sendMulticast(any()) }
                batchResponse.failureCount shouldBe 0
            }
        }

        `when`("빈 디바이스 토큰 리스트로 멀티캐스트를 전송하면") {
            val emptyTokens = emptyList<String>()
            val batchResponse = mockk<BatchResponse> {
                every { failureCount } returns 0
                every { responses } returns emptyList()
            }
            every { firebaseMessaging.sendMulticast(any()) } returns batchResponse

            then("IllegalArgumentException이 발생해야 한다.") {
                shouldThrow<IllegalArgumentException> {
                    firebasePushService.sendPushNotifications(
                        deviceTokens = emptyTokens,
                        title = title,
                        body = body,
                        image = null,
                        data = emptyMap()
                    )
                }
            }
        }
    }

    given("멀티캐스트에서 일부 전송이 실패하는 상황에서") {
        val deviceTokens = listOf("token1", "token2", "token3")
        val title = "멀티캐스트 제목"
        val body = "멀티캐스트 내용"

        `when`("일부 디바이스 전송이 실패하면") {
            val successResponse = mockk<SendResponse> {
                every { isSuccessful } returns true
            }
            val failureResponse = mockk<SendResponse> {
                every { isSuccessful } returns false
                every { exception } returns mockk<FirebaseMessagingException>()
            }
            val batchResponse = mockk<BatchResponse> {
                every { failureCount } returns 1
                every { responses } returns listOf(successResponse, failureResponse, successResponse)
            }
            every { firebaseMessaging.sendMulticast(any()) } returns batchResponse

            then("실패 정보가 로깅되고 처리가 완료되어야 한다") {
                firebasePushService.sendPushNotifications(
                    deviceTokens = deviceTokens,
                    title = title,
                    body = body,
                    image = null,
                    data = emptyMap()
                )

                verify { firebaseMessaging.sendMulticast(any()) }
                batchResponse.failureCount shouldBe 1
            }
        }

        `when`("모든 디바이스 전송이 실패하면") {
            val failureResponse1 = mockk<SendResponse> {
                every { isSuccessful } returns false
                every { exception } returns mockk<FirebaseMessagingException>()
            }
            val failureResponse2 = mockk<SendResponse> {
                every { isSuccessful } returns false
                every { exception } returns mockk<FirebaseMessagingException>()
            }
            val failureResponse3 = mockk<SendResponse> {
                every { isSuccessful } returns false
                every { exception } returns mockk<FirebaseMessagingException>()
            }
            val batchResponse = mockk<BatchResponse> {
                every { failureCount } returns 3
                every { responses } returns listOf(failureResponse1, failureResponse2, failureResponse3)
            }
            every { firebaseMessaging.sendMulticast(any()) } returns batchResponse

            then("모든 실패가 로깅되고 처리가 완료되어야 한다") {
                firebasePushService.sendPushNotifications(
                    deviceTokens = deviceTokens,
                    title = title,
                    body = body,
                    image = null,
                    data = emptyMap()
                )

                verify { firebaseMessaging.sendMulticast(any()) }
                batchResponse.failureCount shouldBe 3
            }
        }

        `when`("실패 응답에 null 예외가 포함되면") {
            val failureResponse = mockk<SendResponse> {
                every { isSuccessful } returns false
                every { exception } returns null
            }
            val batchResponse = mockk<BatchResponse> {
                every { failureCount } returns 1
                every { responses } returns listOf(failureResponse)
            }
            every { firebaseMessaging.sendMulticast(any()) } returns batchResponse

            then("null 예외에 대해서도 안전하게 처리되어야 한다") {
                firebasePushService.sendPushNotifications(
                    deviceTokens = listOf("invalid-token"),
                    title = title,
                    body = body,
                    image = null,
                    data = emptyMap()
                )

                verify { firebaseMessaging.sendMulticast(any()) }
                batchResponse.failureCount shouldBe 1
            }
        }
    }

    given("Firebase 멀티캐스트에서 시스템 오류가 발생하는 상황에서") {
        val deviceTokens = listOf("token1", "token2", "token3")
        val title = "멀티캐스트 제목"
        val body = "멀티캐스트 내용"

        `when`("멀티캐스트 전송 중 예외가 발생하면") {
            val exception = RuntimeException("Multicast error")
            every { firebaseMessaging.sendMulticast(any()) } throws exception

            then("예외를 안전하게 처리하고 메서드가 정상 완료되어야 한다") {
                firebasePushService.sendPushNotifications(
                    deviceTokens = deviceTokens,
                    title = title,
                    body = body,
                    image = null,
                    data = emptyMap()
                )

                verify { firebaseMessaging.sendMulticast(any()) }
            }
        }
    }
})
