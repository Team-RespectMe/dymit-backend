package net.noti_me.dymit.dymit_backend_api.units.configs

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.string.shouldContain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ObservabilityEndpointIntegrationTest : BehaviorSpec() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("Actuator observability endpoints") {
            When("the liveness probe is requested") {
                Then("it reports the application as UP") {
                    webTestClient.get()
                        .uri("/actuator/health/liveness")
                        .exchange()
                        .expectStatus().isOk
                        .expectBody()
                        .jsonPath("$.status").isEqualTo("UP")
                }
            }

            When("the readiness probe is requested") {
                Then("it reports the application as UP") {
                    webTestClient.get()
                        .uri("/actuator/health/readiness")
                        .exchange()
                        .expectStatus().isOk
                        .expectBody()
                        .jsonPath("$.status").isEqualTo("UP")
                }
            }

            When("the Prometheus scrape endpoint is requested after an HTTP request") {
                Then("it exposes Prometheus text containing server, JVM, and process metrics") {
                    webTestClient.get()
                        .uri("/api/v1/health-check")
                        .exchange()
                        .expectStatus().isOk

                    val response = webTestClient.get()
                        .uri("/actuator/prometheus")
                        .exchange()
                        .expectStatus().isOk
                        .expectHeader().contentTypeCompatibleWith(org.springframework.http.MediaType.TEXT_PLAIN)
                        .expectBody(String::class.java)
                        .returnResult()
                        .responseBody.orEmpty()

                    response shouldContain "http_server_requests_seconds_count"
                    response shouldContain "jvm_memory_used_bytes"
                    response shouldContain "process_uptime_seconds"
                }
            }
        }
    }
}
