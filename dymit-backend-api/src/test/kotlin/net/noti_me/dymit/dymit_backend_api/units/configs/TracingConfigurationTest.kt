package net.noti_me.dymit.dymit_backend_api.units.configs

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.micrometer.tracing.Tracer
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
internal class TracingConfigurationTest : BehaviorSpec() {

    @Autowired
    private lateinit var tracer: Tracer

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("Micrometer tracing configuration") {
            When("the application context starts") {
                Then("it exposes a tracer bean") {
                    tracer.shouldNotBeNull()
                }
            }

            When("a span is put in scope") {
                Then("traceId and spanId are correlated into MDC") {
                    val span = tracer.nextSpan().name("task-87-test").start()

                    tracer.withSpan(span).use {
                        MDC.get("traceId").shouldNotBeNull()
                        MDC.get("spanId").shouldNotBeNull()
                    }

                    span.end()
                }
            }
        }
    }
}
