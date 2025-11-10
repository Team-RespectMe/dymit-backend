package net.noti_me.dymit.dymit_backend_api.controllers

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Push Test Controller", description = "Controller for testing push notifications")
class PushTestController(
    private val app: FirebaseApp
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/api/v1/push-test")
    @ResponseStatus(HttpStatus.OK)
    fun pushTest(@RequestParam("token") token: String) {
        // app list 출력
        FirebaseApp.getApps().forEach {
            logger.info("Firebase App Name: ${it.name}")
        }

        val notification = Notification.builder()
            .setTitle("Test Notification")
            .setBody("This is a test push notification.")
            .build()
        val message = MulticastMessage.builder()
            .addAllTokens(listOf(token))
            .setNotification(notification)
            .build()
        try {
            val response = FirebaseMessaging.getInstance(app)
                .sendEachForMulticast(message)
            logger.info("Push test sent successfully: $response")
        } catch(e: Exception) {
            logger.error("Error sending push test ${e.message}", e)
        }
    }
}

