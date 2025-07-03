package net.noti_me.dymit.dymit_backend_api.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import java.util.Optional

@Configuration
@EnableMongoAuditing
class MongoConfig {

}