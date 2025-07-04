package net.noti_me.dymit.dymit_backend_api.configs

import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMemberResolver
import org.apache.juli.logging.Log
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AppConfig : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(LoginMemberResolver())
        super.addArgumentResolvers(resolvers)
    }
}