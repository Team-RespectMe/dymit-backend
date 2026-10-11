package net.noti_me.dymit.dymit_backend_api.units.task.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.common.advice.EnvelopPatternAdvice
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import net.noti_me.dymit.dymit_backend_api.task.adapter.`in`.web.TaskController
import net.noti_me.dymit.dymit_backend_api.task.application.TaskService
import net.noti_me.dymit.dymit_backend_api.task.application.ManagedTaskCursor
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.ManagedTaskDto
import net.noti_me.dymit.dymit_backend_api.task.application.port.`in`.dto.ManagedTaskGroupDto
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
import java.time.LocalDate

internal class ManagedTaskResponseTest : BehaviorSpec({
    afterEach { RequestContextHolder.resetRequestAttributes() }
    listOf(3, 2, 0).forEach { count ->
        Given("관리 과제 후보 $count 개") {
            Then("공개 envelope와 마지막 반환 과제의 복합 커서 next 링크를 만든다") {
                val service = mockk<TaskService>()
                val controller = TaskController(service)
                val member = createMemberInfo(createMemberEntity())
                val rows = List(count) { i ->
                    val id = ObjectId()
                    val at = Instant.parse("2026-10-11T01:00:00Z")
                    ManagedTaskDto(ManagedTaskGroupDto(ObjectId().toHexString(), "소유 그룹 $i"), id.toHexString(), "과제 $i", at, i.toLong(), ManagedTaskCursor(LocalDate.of(2026,10,11), at, id).encode())
                }
                every { service.getManagedTasks(member, null, 2) } returns rows
                val request = MockHttpServletRequest("GET", "/api/v1/study-groups/managed/tasks")
                request.serverName = "localhost"
                request.serverPort = 8080
                request.servletPath = requireNotNull(request.requestURI)
                RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
                val result = controller.getManagedTasks(member, null, 2)
                val method = TaskController::class.java.methods.single { it.name == "getManagedTasks" }
                method.getAnnotation(GetMapping::class.java).value.toList() shouldBe listOf("/managed/tasks")
                method.parameters[1].getAnnotation(RequestParam::class.java).required shouldBe false
                method.parameters[2].getAnnotation(RequestParam::class.java).defaultValue shouldBe "20"
                val envelope = EnvelopPatternAdvice().beforeBodyWrite(result, MethodParameter(method,-1), MediaType.APPLICATION_JSON, MappingJackson2HttpMessageConverter::class.java, mockk(), mockk(relaxed=true))
                val mapper = jacksonObjectMapper().findAndRegisterModules()
                val json = mapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(envelope)
                json["status"].asInt() shouldBe 200
                json["data"]["count"].asInt() shouldBe minOf(count,2)
                json["data"]["items"].size() shouldBe minOf(count,2)
                json["data"]["items"].forEachIndexed { i, row ->
                    row["group"].fieldNames().asSequence().toSet() shouldBe setOf("id", "name")
                    row["group"]["id"].asText() shouldBe rows[i].group.id
                    row["group"]["name"].asText() shouldBe rows[i].group.name
                    row["taskId"].asText() shouldBe rows[i].taskId
                    row["title"].asText() shouldBe rows[i].title
                    mapper.treeToValue(row["expireAt"], Instant::class.java) shouldBe rows[i].expireAt
                    row["notSubmittedAssigneeCount"].asLong() shouldBe i.toLong()
                    row.has("cursor") shouldBe false
                    row.has("groupId") shouldBe false
                    row.has("membershipId") shouldBe false
                }
                if(count > 2) json["data"]["_links"]["next"]["href"].asText() shouldBe "http://localhost:8080${request.servletPath}?cursor=${rows[1].cursor}&size=2"
                else json["data"]["_links"].has("next") shouldBe false
                verify(exactly=1) { service.getManagedTasks(member,null,2) }
            }
        }
    }
})
