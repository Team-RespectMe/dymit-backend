package net.noti_me.dymit.dymit_backend_api.configs

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
class FirebaseConfig(
    @Value("\${firebase.config.path}")
    private val configPath: String
) {

    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount = FileInputStream(configPath)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()
        return if(FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }
}