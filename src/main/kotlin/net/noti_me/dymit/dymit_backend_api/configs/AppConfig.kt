package net.noti_me.dymit.dymit_backend_api.configs

import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMemberResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.Executor

@Configuration
class AppConfig () : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(LoginMemberResolver())
//        resolvers.add(sanitizeHandlerMethodArgumentResolver)
    }

    @Bean
    fun asyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.setThreadNamePrefix("async-executor-")
        executor.maxPoolSize = 10
        executor.corePoolSize = 5
        executor.queueCapacity = 100
        executor.initialize()
        return executor
    }
}