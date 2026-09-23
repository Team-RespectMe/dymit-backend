package net.noti_me.dymit.dymit_backend_api.units.configs

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.configs.JacksonConfig
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.Instant

internal class JacksonConfigTest : BehaviorSpec({

    Given("애플리케이션에 설정된 Jackson ObjectMapper") {
        When("오프셋 없는 밀리초 단위 시각을 Instant로 역직렬화하면") {
            Then("한국 표준시를 적용한 UTC 시각이 된다") {
                withApplicationObjectMapper { objectMapper ->
                    objectMapper.readValue(
                        "\"2026-10-23T10:00:00.000\"",
                        Instant::class.java
                    ) shouldBe Instant.parse("2026-10-23T01:00:00Z")
                }
            }
        }

        When("Z로 끝나는 시각을 Instant로 역직렬화하면") {
            Then("입력된 UTC 시각이 그대로 유지된다") {
                withApplicationObjectMapper { objectMapper ->
                    objectMapper.readValue(
                        "\"2026-10-23T01:00:00.123Z\"",
                        Instant::class.java
                    ) shouldBe Instant.parse("2026-10-23T01:00:00.123Z")
                }
            }
        }

        When("명시적인 오프셋이 있는 시각을 Instant로 역직렬화하면") {
            Then("입력된 오프셋을 기준으로 UTC 시각을 계산한다") {
                withApplicationObjectMapper { objectMapper ->
                    objectMapper.readValue(
                        "\"2026-10-23T10:00:00.456+02:00\"",
                        Instant::class.java
                    ) shouldBe Instant.parse("2026-10-23T08:00:00.456Z")
                }
            }
        }

        When("오프셋 없는 초 단위 시각을 Instant로 역직렬화하면") {
            Then("한국 표준시를 적용하고 분수 초 없이 처리한다") {
                withApplicationObjectMapper { objectMapper ->
                    objectMapper.readValue(
                        "\"2026-10-23T10:00:00\"",
                        Instant::class.java
                    ) shouldBe Instant.parse("2026-10-23T01:00:00Z")
                }
            }
        }

        When("유효하지 않은 시각 문자열을 Instant로 역직렬화하면") {
            Then("Jackson 예외가 발생한다") {
                withApplicationObjectMapper { objectMapper ->
                    shouldThrow<JsonProcessingException> {
                        objectMapper.readValue(
                            "\"유효하지 않은 시각\"",
                            Instant::class.java
                        )
                    }
                }
            }
        }

        When("Instant를 JSON으로 직렬화하면") {
            Then("기존 UTC API 형식으로 출력한다") {
                withApplicationObjectMapper { objectMapper ->
                    objectMapper.writeValueAsString(
                        Instant.parse("2026-10-23T01:00:00.123Z")
                    ) shouldBe "\"2026-10-23T01:00:00.123Z\""
                }
            }
        }
    }
})

private fun withApplicationObjectMapper(assertion: (ObjectMapper) -> Unit) {
    ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration::class.java))
        .withUserConfiguration(JacksonConfig::class.java)
        .run { context -> assertion(context.getBean(ObjectMapper::class.java)) }
}
