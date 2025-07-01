package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.slf4j.LoggerFactory

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun httpSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf{ it.disable() }
            .formLogin { it.disable() }
            .cors { it.disable() }
            .sessionManagement{ it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.anyRequest().permitAll()
            }

        return http.build()
    }
}
