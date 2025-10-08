package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

@Configuration
class JacksonConfig {

    @Bean
    fun jacksonObjectMapperBuilderCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        customizer ->
        customizer.timeZone(TimeZone.getTimeZone("Asia/Seoul"))
        customizer.simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    }
}