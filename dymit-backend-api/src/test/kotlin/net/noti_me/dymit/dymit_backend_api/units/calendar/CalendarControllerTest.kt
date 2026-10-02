package net.noti_me.dymit.dymit_backend_api.units.calendar

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.calendar.adapter.`in`.web.CalendarController
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.*
import net.noti_me.dymit.dymit_backend_api.calendar.application.usecase.dto.*
import net.noti_me.dymit.dymit_backend_api.calendar.domain.CalendarEventType
import net.noti_me.dymit.dymit_backend_api.common.advice.EnvelopPatternAdvice
import net.noti_me.dymit.dymit_backend_api.common.advice.GlobalErrorHandlerAdvice
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.springframework.core.MethodParameter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.Instant
import java.time.LocalDate

internal class CalendarControllerTest : BehaviorSpec() {

    private val weekly = mockk<GetCalendarWeeklyPresenceUseCase>()
    private val daily = mockk<GetCalendarDailyEventsUseCase>()
    private val member = createMemberInfo(createMemberEntity())
    private val mapper = jacksonObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    private val mvc = MockMvcBuilders.standaloneSetup(CalendarController(weekly, daily))
        .setControllerAdvice(GlobalErrorHandlerAdvice(), EnvelopPatternAdvice())
        .setMessageConverters(MappingJackson2HttpMessageConverter(mapper))
        .setCustomArgumentResolvers(object : HandlerMethodArgumentResolver {
            override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == MemberInfo::class.java
            override fun resolveArgument(
                parameter: MethodParameter,
                mavContainer: ModelAndViewContainer?,
                webRequest: NativeWebRequest,
                binderFactory: WebDataBinderFactory?
            ): Any = member
        }).build()

    init {
        afterEach { clearAllMocks() }
        Given("주간 API 존재 여부 DTO") {
            Then("7개 날짜 항목은 date와 hasEvents 두 필드만 제공한다") {
                val date = LocalDate.parse("2026-10-02")
                every { weekly.execute(GetCalendarWeeklyPresenceCommand(member.memberId, date)) } returns (0L..6L).map {
                    val itemDate = LocalDate.parse("2026-09-27").plusDays(it)
                    CalendarDayPresenceDto(itemDate, itemDate == date)
                }
                val result = mvc.perform(get("/api/v1/members/me/calendar/weekly-presence").param("date", date.toString()))
                    .andExpect(status().isOk).andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.count").value(7))
                    .andExpect(jsonPath("$.data.items[0].date").value("2026-09-27"))
                    .andExpect(jsonPath("$.data.items[0].hasEvents").value(false))
                    .andExpect(jsonPath("$.data.items[5].hasEvents").value(true))
                    .andExpect(jsonPath("$.data.items[*].groups").doesNotExist())
                    .andExpect(jsonPath("$.data.items[*].events").doesNotExist())
                    .andExpect(jsonPath("$.data._links").isMap).andReturn()
                mapper.readTree(result.response.contentAsString)["data"]["items"].forEach {
                    it.fieldNames().asSequence().toSet() shouldBe setOf("date", "hasEvents")
                }
            }
        }
        Given("일별 API의 여러 그룹에 세 이벤트가 있으면") {
            Then("flat items와 각 이벤트의 group 객체 및 이벤트 수 count를 반환한다") {
                val at = Instant.parse("2026-10-02T00:00:00Z")
                val schedule = CalendarEventDto(CalendarEventType.STUDY_SCHEDULE, "event", "일정", at, CalendarEventGroupDto("a", "첫 그룹"))
                every { daily.execute(any()) } returns listOf(
                    schedule, schedule.copy(type = CalendarEventType.TASK, id = "task", title = "과제"),
                    schedule.copy(id = "third", group = CalendarEventGroupDto("b", "둘째 그룹"))
                )
                val result = mvc.perform(get("/api/v1/members/me/calendar/events").param("date", "2026-10-02"))
                    .andExpect(status().isOk).andExpect(jsonPath("$.data.count").value(3))
                    .andExpect(jsonPath("$.data.items.length()").value(3))
                    .andExpect(jsonPath("$.data.items[0].group.id").value("a"))
                    .andExpect(jsonPath("$.data.items[0].group.name").value("첫 그룹"))
                    .andExpect(jsonPath("$.data.items[0].type").value("STUDY_SCHEDULE"))
                    .andExpect(jsonPath("$.data.items[1].type").value("TASK"))
                    .andExpect(jsonPath("$.data.items[1].id").value("task"))
                    .andExpect(jsonPath("$.data.items[1].title").value("과제"))
                    .andExpect(jsonPath("$.data.items[1].eventAt").value("2026-10-02T00:00:00Z"))
                    .andExpect(jsonPath("$.data.items[2].group.id").value("b"))
                    .andExpect(jsonPath("$.data.items[*].events").doesNotExist()).andReturn()
                mapper.readTree(result.response.contentAsString)["data"]["items"].forEach {
                    it.fieldNames().asSequence().toSet() shouldBe setOf("type", "id", "title", "eventAt", "group")
                    it["group"].fieldNames().asSequence().toSet() shouldBe setOf("id", "name")
                }
            }
        }
        Given("일별 API에 이벤트가 없는 날") {
            Then("count 0과 빈 items를 반환한다") {
                every { daily.execute(any()) } returns emptyList()
                mvc.perform(get("/api/v1/members/me/calendar/events").param("date", "2026-10-02"))
                    .andExpect(status().isOk).andExpect(jsonPath("$.data.count").value(0))
                    .andExpect(jsonPath("$.data.items").isEmpty)
            }
        }
        listOf("weekly-presence", "events").forEach { path ->
            Given("$path API에서 날짜를 생략하면") {
                Then("서울의 오늘을 명령에 전달한다") {
                    val before = LocalDate.now(CalendarQueryDateParser.KOREA_ZONE)
                    every { weekly.execute(any()) } returns emptyList()
                    every { daily.execute(any()) } returns emptyList()
                    mvc.perform(get("/api/v1/members/me/calendar/$path")).andExpect(status().isOk)
                    val after = LocalDate.now(CalendarQueryDateParser.KOREA_ZONE)
                    if (path == "events") {
                        verify { daily.execute(match { it.memberId == member.memberId && it.date in listOf(before, after) }) }
                    } else {
                        verify { weekly.execute(match { it.memberId == member.memberId && it.date in listOf(before, after) }) }
                    }
                }
            }
            Given("$path API의 범위 오류") {
                Then("애플리케이션 날짜 범위 오류를 기존 400 응답으로 변환한다") {
                    val value = "+999999998-10-02"
                    every { weekly.execute(match { it.date == LocalDate.parse(value) }) } throws BadRequestException(message = "범위 오류")
                    every { daily.execute(match { it.date == LocalDate.parse(value) }) } throws BadRequestException(message = "범위 오류")
                    mvc.perform(get("/api/v1/members/me/calendar/$path").param("date", value))
                        .andExpect(status().isBadRequest).andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.status").value(400))
                }
            }
            listOf("not-a-date", "2026-02-30", "").forEach { invalid ->
                Given("$path API의 잘못된 날짜 '$invalid'") {
                    Then("유즈케이스 호출 없이 기존 BAD_REQUEST 오류를 반환한다") {
                        mvc.perform(get("/api/v1/members/me/calendar/$path").param("date", invalid))
                            .andExpect(status().isBadRequest).andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                            .andExpect(jsonPath("$.status").value(400))
                        verify(exactly = 0) { weekly.execute(any()) }
                        verify(exactly = 0) { daily.execute(any()) }
                    }
                }
            }
        }
        Given("캘린더 컨트롤러 인증 계약") {
            Then("양쪽 조회 메서드는 로그인 회원과 MEMBER 또는 ADMIN 역할을 선언한다") {
                listOf("getWeeklyPresence", "getDailyEvents").forEach { name ->
                    val method = CalendarController::class.java.methods.single { it.name == name }
                    method.getAnnotation(RolesAllowed::class.java).value.toList() shouldBe listOf("MEMBER", "ADMIN")
                    method.parameters[0].isAnnotationPresent(LoginMember::class.java) shouldBe true
                }
            }
        }
    }
}
