package net.noti_me.dymit.dymit_backend_api.configs

import com.fasterxml.jackson.databind.ObjectMapper
import net.noti_me.dymit.dymit_backend_api.application.auth.jwt.JwtService
import net.noti_me.dymit.dymit_backend_api.common.security.exceptions.JwtAccessDeniedHandler
import net.noti_me.dymit.dymit_backend_api.common.security.exceptions.JwtEntrypointUnauthorizedHandler
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtAuthenticationFilter
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.JwtAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun entryPointUnauthorizedHandler(objectMapper: ObjectMapper): JwtEntrypointUnauthorizedHandler {
        return JwtEntrypointUnauthorizedHandler(objectMapper)
    }

    @Bean
    fun accessDeniedHandler(objectMapper: ObjectMapper): JwtAccessDeniedHandler {
        return JwtAccessDeniedHandler(objectMapper)
    }

    @Bean
    fun jwtAuthenticationProvider(jwtService: JwtService) : JwtAuthenticationProvider {
        return JwtAuthenticationProvider(jwtService)
    }

    @Bean
    fun authenticationManager(jwtAuthenticationProvider: JwtAuthenticationProvider): AuthenticationManager {
        return ProviderManager(jwtAuthenticationProvider)
    }

    @Bean
    fun httpSecurityFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        entryPointUnauthorizedHandler: JwtEntrypointUnauthorizedHandler,
        accessDeniedHandler: JwtAccessDeniedHandler
    ): SecurityFilterChain {
        http.csrf{ it.disable() }
            .formLogin { it.disable() }
            .cors { it.disable() }
            .sessionManagement{ it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationManager(authenticationManager)
            .addFilterBefore(
                JwtAuthenticationFilter(authenticationManager),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling {
                it.authenticationEntryPoint(entryPointUnauthorizedHandler)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .authorizeHttpRequests { it ->
                it.requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**",
                    "/prometheus/**",
                    "/health-check"
                ).permitAll()
                it.requestMatchers(HttpMethod.POST,
                    "/api/v1/members",
                    "/api/v1/auth/oidc/**",
                    "/api/v1/auth/jwt/**"
                ).permitAll()
                it.requestMatchers("/api/v1/members/nickname-validation")
                    .permitAll()
                it.anyRequest().authenticated()
            }

        return http.build()
    }
}
