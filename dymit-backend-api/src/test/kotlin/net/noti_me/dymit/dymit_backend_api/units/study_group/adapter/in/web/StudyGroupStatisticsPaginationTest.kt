package net.noti_me.dymit.dymit_backend_api.units.study_group.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.advice.EnvelopPatternAdvice
import net.noti_me.dymit.dymit_backend_api.study_group.adapter.`in`.web.StudyGroupController
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetGroupStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.GetGroupMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.StatisticsGroupDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GroupStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpResponse
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant

internal class StudyGroupStatisticsPaginationTest : BehaviorSpec({

    val method = StudyGroupController::class.java.methods.single { it.name == "getGroupMemberStatistics" }
    val returnType = MethodParameter(method, -1)
    afterEach { RequestContextHolder.resetRequestAttributes() }

    Given("구성원 통계 목록 HTTP 계약") {
        Then("cursor는 선택이고 size 기본값은 20이며 통계 목록 경로로 노출된다") {
            method.getAnnotation(GetMapping::class.java).value.toList() shouldBe listOf("/{groupId}/statistics/members")
            method.parameters[2].getAnnotation(RequestParam::class.java).required shouldBe false
            method.parameters[3].getAnnotation(RequestParam::class.java).defaultValue shouldBe "20"
            GetGroupMemberStatisticsCommand("requester", "group").size shouldBe 20
        }
    }

    Given("가입 이력 식별자가 사용자 식별자와 다른 개인 통계") {
        Then("개인 통계 envelop JSON은 사용자 ID를 유지하고 membershipId 필드를 노출하지 않는다") {
            val useCase = mockk<GetMemberStatisticsUseCase>()
            val controller = StudyGroupController(mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), useCase, mockk())
            val member = createMemberInfo(createMemberEntity())
            val group = ObjectId().toHexString()
            val membershipId = "000000000000000000000001"
            val memberId = "100000000000000000000001"
            val command = GetMemberStatisticsCommand(member.memberId, group, memberId)
            every { useCase.execute(command) } returns MemberStatisticsDto(
                group, membershipId, memberId, 12, Instant.EPOCH, StatisticsCounts(1, 2), 50.0, 0.0
            )
            val result = controller.getMemberStatistics(member, group, memberId)
            val personalMethod = StudyGroupController::class.java.methods.single { it.name == "getMemberStatistics" }
            val personalReturnType = MethodParameter(personalMethod, -1)
            val advice = EnvelopPatternAdvice()
            advice.supports(personalReturnType, MappingJackson2HttpMessageConverter::class.java) shouldBe true
            val response = mockk<ServerHttpResponse>(relaxed = true)
            val envelop = advice.beforeBodyWrite(result, personalReturnType, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter::class.java, mockk(), response)
            val json = jacksonObjectMapper().findAndRegisterModules().valueToTree<com.fasterxml.jackson.databind.JsonNode>(envelop)
            json["status"].asInt() shouldBe 200
            json["data"].has("membershipId") shouldBe false
            json["data"]["memberId"].asText() shouldBe memberId
            json["data"]["groupId"].asText() shouldBe group
            json["data"].has("weekEnd") shouldBe false
            json["data"]["latestSession"].asLong() shouldBe 12
            json["data"]["counts"]["submittedTaskCount"].asInt() shouldBe 1
            json["data"]["taskSubmissionRate"].asDouble() shouldBe 50.0
            verify(exactly = 1) { useCase.execute(command) }
            verify(exactly = 1) { response.setStatusCode(HttpStatus.OK) }
        }
    }

    Given("현재 및 이전 주 비율을 가진 그룹 통계") {
        Then("그룹 envelop JSON은 네 비율을 유지하고 서버 차이 필드를 노출하지 않는다") {
            val useCase = mockk<GetGroupStatisticsUseCase>()
            val controller = StudyGroupController(mockk(), mockk(), mockk(), useCase, mockk(), mockk(), mockk(), mockk())
            val member = createMemberInfo(createMemberEntity())
            val group = ObjectId().toHexString()
            val command = GetGroupStatisticsCommand(member.memberId, group)
            every { useCase.execute(command) } returns GroupStatisticsDto(
                group = StatisticsGroupDto(group, "현재 그룹 이름"),
                latestSession = 12,
                previousSession = 7,
                statisticsAt = Instant.EPOCH,
                activeMemberCount = 3,
                counts = StatisticsCounts(2, 3, 1, 3),
                taskSubmissionRate = 66.67,
                scheduleAttendanceRate = 33.33,
                previousTaskSubmissionRate = 50.0,
                previousScheduleAttendanceRate = 25.0
            )
            val result = controller.getGroupStatistics(member, group)
            val groupMethod = StudyGroupController::class.java.methods.single { it.name == "getGroupStatistics" }
            val groupReturnType = MethodParameter(groupMethod, -1)
            val advice = EnvelopPatternAdvice()
            advice.supports(groupReturnType, MappingJackson2HttpMessageConverter::class.java) shouldBe true
            val response = mockk<ServerHttpResponse>(relaxed = true)
            val envelop = advice.beforeBodyWrite(result, groupReturnType, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter::class.java, mockk(), response)
            val json = jacksonObjectMapper().findAndRegisterModules().valueToTree<com.fasterxml.jackson.databind.JsonNode>(envelop)
            json["status"].asInt() shouldBe 200
            json["data"].has("groupId") shouldBe false
            json["data"]["group"]["id"].asText() shouldBe group
            json["data"]["group"]["name"].asText() shouldBe "현재 그룹 이름"
            json["data"]["group"].fieldNames().asSequence().toSet() shouldBe setOf("id", "name")
            json["data"].has("weekEnd") shouldBe false
            json["data"]["latestSession"].asLong() shouldBe 12
            json["data"]["taskSubmissionRate"].asDouble() shouldBe 66.67
            json["data"]["scheduleAttendanceRate"].asDouble() shouldBe 33.33
            json["data"]["previousSession"].asLong() shouldBe 7
            json["data"]["activeMemberCount"].asLong() shouldBe 3
            json["data"]["previousTaskSubmissionRate"].asDouble() shouldBe 50.0
            json["data"]["previousScheduleAttendanceRate"].asDouble() shouldBe 25.0
            json["data"].has("taskSubmissionRateDifferencePp") shouldBe false
            json["data"].has("scheduleAttendanceRateDifferencePp") shouldBe false
            verify(exactly = 1) { useCase.execute(command) }
            verify(exactly = 1) { response.setStatusCode(HttpStatus.OK) }
        }
    }

    listOf(3, 2, 0).forEach { returnedCount ->
        Given("size 2 요청에 통계 $returnedCount 개가 조회된 경우") {
            Then("응답 개수와 항목 및 다음 페이지 링크를 공통 envelop 형식으로 반환한다") {
                val useCase = mockk<GetGroupMemberStatisticsUseCase>()
                val controller = StudyGroupController(mockk(), mockk(), mockk(), mockk(), mockk(), useCase, mockk(), mockk())
                val member = createMemberInfo(createMemberEntity())
                val group = ObjectId().toHexString()
                val cursor = ObjectId().toHexString()
                val items = (1..returnedCount).map { index ->
                    MemberStatisticsDto(group, ObjectId("00000000000000000000000$index").toHexString(),
                        ObjectId("10000000000000000000000$index").toHexString(),
                        12, Instant.EPOCH, StatisticsCounts(1, 2), 50.0, 0.0)
                }
                every { useCase.execute(GetGroupMemberStatisticsCommand(member.memberId, group, cursor, 2)) } returns items
                val request = MockHttpServletRequest("GET", "/api/v1/study-groups/$group/statistics/members")
                request.serverName = "localhost"
                request.serverPort = 8080
                request.servletPath = requireNotNull(request.requestURI)
                RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

                val result = controller.getGroupMemberStatistics(member, group, cursor, 2)
                result.count shouldBe minOf(returnedCount, 2).toLong()
                result.items.map { it.memberId } shouldBe items.take(2).map { it.memberId }
                val advice = EnvelopPatternAdvice()
                advice.supports(returnType, MappingJackson2HttpMessageConverter::class.java) shouldBe true
                val response = mockk<ServerHttpResponse>(relaxed = true)
                val envelop = advice.beforeBodyWrite(result, returnType, MediaType.APPLICATION_JSON,
                    MappingJackson2HttpMessageConverter::class.java, mockk(), response)
                val json = jacksonObjectMapper().findAndRegisterModules().valueToTree<com.fasterxml.jackson.databind.JsonNode>(envelop)
                json["status"].asInt() shouldBe 200
                json["data"]["count"].asInt() shouldBe minOf(returnedCount, 2)
                json["data"]["items"].size() shouldBe minOf(returnedCount, 2)
                json["data"].has("nextCursorMembershipId") shouldBe false
                json["data"]["items"].forEachIndexed { index, item ->
                    item.has("membershipId") shouldBe false
                    item["memberId"].asText() shouldBe items[index].memberId
                }
                if (returnedCount > 2) {
                    json["data"]["_links"]["next"]["href"].asText() shouldBe
                        "http://localhost:8080${request.servletPath}?cursor=${items[1].membershipId}&size=2"
                    json["data"]["items"][0]["counts"]["submittedTaskCount"].asInt() shouldBe 1
                } else {
                    json["data"]["_links"].has("next") shouldBe false
                }
                verify(exactly = 1) { response.setStatusCode(HttpStatus.OK) }
                verify(exactly = 1) { useCase.execute(GetGroupMemberStatisticsCommand(member.memberId, group, cursor, 2)) }
            }
        }
    }
})
