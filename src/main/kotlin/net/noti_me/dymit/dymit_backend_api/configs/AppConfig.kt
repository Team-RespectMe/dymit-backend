package net.noti_me.dymit.dymit_backend_api.configs

import com.fasterxml.jackson.databind.ObjectMapper
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMemberResolver
import net.noti_me.dymit.dymit_backend_api.common.logging.LogReporter
import net.noti_me.dymit.dymit_backend_api.common.logging.discord.DiscordMessageReporter
import org.apache.juli.logging.Log
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AppConfig : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(LoginMemberResolver())
        super.addArgumentResolvers(resolvers)
    }
}