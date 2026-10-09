package net.noti_me.dymit.dymit_backend_api.units.study_group.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.common.advice.EnvelopPatternAdvice
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.`in`.web.StudyGroupController
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetManagedGroupStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetManagedGroupStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.StatisticsGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.ManagedGroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant

internal class ManagedGroupStatisticsResponseTest : BehaviorSpec({

    afterEach { RequestContextHolder.resetRequestAttributes() }
    listOf(3, 2, 0).forEach { count ->
        Given("관리 그룹 통계 후보 $count 개와 페이지 size 2") {
            Then("공통 응답과 공개 metadata를 반환하며 마지막 반환 그룹 ID로 다음 링크를 만든다") {
                val useCase = mockk<GetManagedGroupStatisticsUseCase>()
                val controller = StudyGroupController(mockk(), mockk(), mockk(), mockk(), useCase, mockk(), mockk(), mockk())
                val member = createMemberInfo(createMemberEntity())
                val cursor = ObjectId().toHexString()
                val rows = List(count) { index -> ManagedGroupStatisticsDto(
                    StatisticsGroupDto(ObjectId().toHexString(), "현재 그룹 $index"), if (index == 0) 12 else null,
                    if (index == 0) Instant.EPOCH else null, 3, if (index == 0) 50.0 else 0.0, 25.0, true
                ) }
                every { useCase.execute(GetManagedGroupStatisticsCommand(member.memberId, cursor, 2)) } returns rows
                val request = MockHttpServletRequest("GET", "/api/v1/study-groups/statistics")
                request.serverName = "localhost"
                request.serverPort = 8080
                request.servletPath = requireNotNull(request.requestURI)
                RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
                val result = controller.getManagedGroupStatistics(member, cursor, 2)
                val method = StudyGroupController::class.java.methods.single { it.name == "getManagedGroupStatistics" }
                method.getAnnotation(GetMapping::class.java).value.toList() shouldBe listOf("/statistics")
                method.parameters[1].getAnnotation(RequestParam::class.java).required shouldBe false
                method.parameters[2].getAnnotation(RequestParam::class.java).defaultValue shouldBe "20"
                val envelop = EnvelopPatternAdvice().beforeBodyWrite(result, MethodParameter(method, -1), MediaType.APPLICATION_JSON,
                    MappingJackson2HttpMessageConverter::class.java, mockk(), mockk(relaxed = true))
                val json = jacksonObjectMapper().findAndRegisterModules().valueToTree<com.fasterxml.jackson.databind.JsonNode>(envelop)
                json["status"].asInt() shouldBe 200
                json["data"]["count"].asInt() shouldBe minOf(count, 2)
                json["data"]["items"].size() shouldBe minOf(count, 2)
                json["data"]["items"].forEachIndexed { index, row ->
                    row.has("groupId") shouldBe false
                    row["group"]["id"].asText() shouldBe rows[index].group.id
                    row["group"]["name"].asText() shouldBe rows[index].group.name
                    row["group"].fieldNames().asSequence().toSet() shouldBe setOf("id", "name")
                    row["activeMemberCount"].asLong() shouldBe 3
                    row["hasSchedule"].asBoolean() shouldBe true
                    row.has("membershipId") shouldBe false
                    row.has("weekEnd") shouldBe false
                    row.has("taskSubmissionRateDifferencePp") shouldBe false
                    row.has("scheduleAttendanceRateDifferencePp") shouldBe false
                    if (index == 1) {
                        row["latestSession"].isNull shouldBe true
                        row["statisticsAt"].isNull shouldBe true
                    }
                }
                if (count > 2) {
                    json["data"]["_links"]["next"]["href"].asText() shouldBe
                        "http://localhost:8080${request.servletPath}?cursor=${rows[1].group.id}&size=2"
                } else {
                    json["data"]["_links"].has("next") shouldBe false
                }
            }
        }
    }
})
