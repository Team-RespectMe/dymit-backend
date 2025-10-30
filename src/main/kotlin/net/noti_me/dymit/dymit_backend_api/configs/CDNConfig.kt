package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class CDNConfig(
    @Value("\${cdn.domain}") private val cdnDomain: String
) {
    fun getDomain() = cdnDomain
}

