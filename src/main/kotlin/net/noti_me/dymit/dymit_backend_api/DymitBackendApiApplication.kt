package net.noti_me.dymit.dymit_backend_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing

@SpringBootApplication
//@EnableMongoAuditing
class DymitBackendApiApplication

fun main(args: Array<String>) {
	runApplication<DymitBackendApiApplication>(*args)
}
