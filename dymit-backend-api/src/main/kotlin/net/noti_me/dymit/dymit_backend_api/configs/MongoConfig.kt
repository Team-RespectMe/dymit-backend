package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.Optional

@Configuration
@EnableMongoAuditing
@EnableTransactionManagement
class MongoConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAware {
            // Return a fixed auditor for simplicity; in a real application, this would be dynamic
            Optional.of("system")
        }
    }

    @Bean
    fun mongoTransactionManager(
        mongoDatabaseFactory: MongoDatabaseFactory
    ): MongoTransactionManager {
        return MongoTransactionManager(mongoDatabaseFactory)
    }
}
