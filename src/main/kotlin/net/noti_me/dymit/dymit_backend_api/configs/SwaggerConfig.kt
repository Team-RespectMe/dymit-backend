package net.noti_me.dymit.dymit_backend_api.configs

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig(
    @Value("\${swagger.domain}")
    val domain: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("SwaggerConfig initialized with domain: $domain")
    }

    @Bean
    fun openApiConfig(): OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("Dymit Backend API Documentation")
                .description("Dymit 백엔드 API 문서입니다. 이 문서는 Dymit의 백엔드 API를 설명합니다.")
            )
            .components(
                Components().addSecuritySchemes("bearer-jwt",
                    SecurityScheme().type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
            ))
            .addSecurityItem(SecurityRequirement().addList("bearer-jwt"))
            .addServersItem(Server().url("$domain"))
    }

}