package net.noti_me.dymit.dymit_backend_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
