package net.noti_me.dymit.dymit_backend_api.units.feed.adapter.`in`.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import jakarta.annotation.security.RolesAllowed
import net.noti_me.dymit.dymit_backend_api.common.advice.EnvelopPatternAdvice
import net.noti_me.dymit.dymit_backend_api.common.annotation.LoginMember
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.feed.adapter.`in`.web.UserFeedController
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.*
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.*
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.springframework.core.MethodParameter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.*

internal class FeedUnreadControllerTest : BehaviorSpec({
    listOf(0L, 3L).forEach { count ->
        Given("인증 회원의 미읽음 수 $count") {
            Then("GET 실제 HTTP 응답 envelope data에 unreadCount만 반환한다") {
                val counter=mockk<GetUnreadUserFeedCountUseCase>()
                val member=createMemberInfo(createMemberEntity())
                every { counter.execute(GetUnreadUserFeedCountCommand(member.memberId)) } returns count
                val controller=UserFeedController(mockk(),mockk(),mockk(),counter,mockk())
                val mvc=MockMvcBuilders.standaloneSetup(controller)
                    .setControllerAdvice(EnvelopPatternAdvice())
                    .setCustomArgumentResolvers(object:HandlerMethodArgumentResolver {
                        override fun supportsParameter(parameter:MethodParameter)=parameter.parameterType==MemberInfo::class.java
                        override fun resolveArgument(parameter:MethodParameter,mavContainer:ModelAndViewContainer?,webRequest:NativeWebRequest,binderFactory:org.springframework.web.bind.support.WebDataBinderFactory?)=member
                    })
                    .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper().findAndRegisterModules()))
                    .build()
                val response=mvc.perform(get("/api/v1/user-feeds/unread-count")).andReturn().response
                response.status shouldBe 200
                val json=jacksonObjectMapper().readTree(response.contentAsString)
                json["status"].asInt() shouldBe 200
                json["data"].fieldNames().asSequence().toSet() shouldBe setOf("unreadCount")
                json["data"]["unreadCount"].asLong() shouldBe count
                val method=UserFeedController::class.java.methods.single { it.name=="getUnreadUserFeedCount" }
                method.parameters.single().isAnnotationPresent(LoginMember::class.java) shouldBe true
                method.getAnnotation(RolesAllowed::class.java).value.toList() shouldBe listOf("MEMBER","ADMIN")
                verify(exactly=1) { counter.execute(GetUnreadUserFeedCountCommand(member.memberId)) }
            }
        }
    }
    Given("인증 회원의 모두 읽음 HTTP 요청") {
        Then("본문 없이 PATCH를 받아 회원 명령을 전달하고 204 빈 응답을 반환한다") {
            val reader=mockk<MarkAllUserFeedsAsReadUseCase>()
            val member=createMemberInfo(createMemberEntity())
            every { reader.execute(MarkAllUserFeedsAsReadCommand(member.memberId)) } just Runs
            val controller=UserFeedController(mockk(),mockk(),mockk(),mockk(),reader)
            val mvc=MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(EnvelopPatternAdvice())
                .setCustomArgumentResolvers(object:HandlerMethodArgumentResolver {
                    override fun supportsParameter(parameter:MethodParameter)=parameter.parameterType==MemberInfo::class.java
                    override fun resolveArgument(parameter:MethodParameter,mavContainer:ModelAndViewContainer?,webRequest:NativeWebRequest,binderFactory:org.springframework.web.bind.support.WebDataBinderFactory?)=member
                }).build()
            val response=mvc.perform(patch("/api/v1/user-feeds/read")).andReturn().response
            response.status shouldBe 204
            response.contentAsString shouldBe ""
            val method=UserFeedController::class.java.methods.single { it.name=="markAllUserFeedsAsRead" }
            method.parameters.size shouldBe 1
            method.parameters.single().isAnnotationPresent(RequestBody::class.java) shouldBe false
            method.parameters.single().isAnnotationPresent(LoginMember::class.java) shouldBe true
            verify(exactly=1) { reader.execute(MarkAllUserFeedsAsReadCommand(member.memberId)) }
        }
    }
})
