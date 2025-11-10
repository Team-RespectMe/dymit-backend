package net.noti_me.dymit.dymit_backend_api.configs

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
class FirebaseConfig(
    @Value("\${firebase.config.path}")
    private val configPath: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount = FileInputStream(configPath)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()
        return if(FirebaseApp.getApps().isEmpty()) {
            logger.info("Initializing Firebase App with config at $configPath")
            FirebaseApp.initializeApp(options)
        } else {
            logger.info("Firebase App already initialized, using existing instance")
            FirebaseApp.getInstance()
        }
    }
}
