package net.noti_me.dymit.dymit_backend_api.units.common.pagination

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.common.pagination.CursorNextUrlBuilder
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

internal class CursorNextUrlBuilderTest : BehaviorSpec() {

    init {
        afterEach {
            RequestContextHolder.resetRequestAttributes()
        }

        Given("localhost direct request with non-default port") {
            Then("it includes the port") {
                bindRequest(
                    serverName = "localhost",
                    serverPort = 8080
                )

                nextUrl() shouldBe "http://localhost:8080/api/test?cursor=b"
            }
        }

        Given("127.0.0.1 direct request with non-default port") {
            Then("it includes the port") {
                bindRequest(
                    serverName = "127.0.0.1",
                    serverPort = 3000
                )

                nextUrl() shouldBe "http://127.0.0.1:3000/api/test?cursor=b"
            }
        }

        Given("localhost direct request with default port 80") {
            Then("it omits the port") {
                bindRequest(
                    serverName = "localhost",
                    serverPort = 80
                )

                nextUrl() shouldBe "http://localhost/api/test?cursor=b"
            }
        }

        Given("forwarded local request with port 443") {
            Then("it keeps the port because localhost uses http scheme") {
                bindRequest(
                    serverName = "internal-service",
                    serverPort = 8080,
                    forwardedHost = "localhost",
                    forwardedPort = "443"
                )

                nextUrl() shouldBe "http://localhost:443/api/test?cursor=b"
            }
        }

        Given("non-local direct request") {
            Then("it omits the direct server port") {
                bindRequest(
                    serverName = "api.example.com",
                    serverPort = 8443
                )

                nextUrl() shouldBe "https://api.example.com/api/test?cursor=b"
            }
        }

        Given("non-local forwarded request with X-Forwarded-Port") {
            Then("it omits the forwarded port") {
                bindRequest(
                    serverName = "internal-service",
                    serverPort = 8080,
                    forwardedHost = "api.example.com",
                    forwardedPort = "9443"
                )

                nextUrl() shouldBe "https://api.example.com/api/test?cursor=b"
            }
        }

        Given("non-local forwarded host with embedded port") {
            Then("it normalizes the host and omits the embedded port") {
                bindRequest(
                    serverName = "internal-service",
                    serverPort = 8080,
                    forwardedHost = "api.example.com:9443"
                )

                nextUrl() shouldBe "https://api.example.com/api/test?cursor=b"
            }
        }

        Given("forwarded local request with non-default port") {
            Then("it prefers and includes the forwarded port") {
                bindRequest(
                    serverName = "internal-service",
                    serverPort = 8080,
                    forwardedHost = "127.0.0.1",
                    forwardedPort = "9090"
                )

                nextUrl() shouldBe "http://127.0.0.1:9090/api/test?cursor=b"
            }
        }
    }

    private fun bindRequest(
        serverName: String,
        serverPort: Int,
        forwardedHost: String? = null,
        forwardedPort: String? = null
    ) {
        val request = MockHttpServletRequest("GET", "/api/test").apply {
            this.serverName = serverName
            this.serverPort = serverPort
            servletPath = "/api/test"
            contextPath = ""
            forwardedHost?.let { addHeader("X-Forwarded-Host", it) }
            forwardedPort?.let { addHeader("X-Forwarded-Port", it) }
        }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    private fun nextUrl(): String? {
        return CursorNextUrlBuilder.buildNextUrlWithExtractor(
            items = listOf("a", "b", "c"),
            extractors = mapOf("cursor" to { value: String -> value }),
            size = 2
        )
    }
}
