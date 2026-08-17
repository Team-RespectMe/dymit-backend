package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.`in`.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.web.StudyRecruitmentController
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.QueryStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.StudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.StudyRecruitmentApi
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/** Study recruitment web adapter unit tests. */
internal class StudyRecruitmentControllerTest : BehaviorSpec() {

    private val queryStudyRecruitmentUseCase = mockk<QueryStudyRecruitmentUseCase>()
    private val controller = StudyRecruitmentController(queryStudyRecruitmentUseCase)
    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "member",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )

    init {
        afterEach { RequestContextHolder.resetRequestAttributes() }

        Given("a recruitment list request with a following page") {
            val requestedSize = 2
            val returnedDtos = listOf(createDto("one"), createDto("two"), createDto("three"))
            every {
                queryStudyRecruitmentUseCase.execute(QueryStudyRecruitmentCommand(cursor = null, size = requestedSize))
            } returns returnedDtos
            RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request()))

            When("the web adapter handles the request") {
                val response = controller.getStudyRecruitments(memberInfo, null, requestedSize)

                Then("it converts the query, maps DTOs, and returns the requested page with its cursor link") {
                    verify(exactly = 1) {
                        queryStudyRecruitmentUseCase.execute(QueryStudyRecruitmentCommand(cursor = null, size = requestedSize))
                    }
                    response.count shouldBe 2L
                    response.items.map { it.externalId } shouldBe listOf("external-one", "external-two")
                    response._links["next"]?.href?.contains("cursor=two") shouldBe true
                    response._links["next"]?.href?.contains("size=2") shouldBe true
                }
            }
        }

        Given("the relocated v1 web contract") {
            Then("the API interface and response DTO live under the application port web package") {
                StudyRecruitmentApi::class.java.`package`.name shouldBe
                    "net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.in.web"
                responseDtoFieldNames() shouldContainAll listOf(
                    "id",
                    "externalId",
                    "type",
                    "title",
                    "content",
                    "url",
                    "writer",
                    "createdAt",
                    "updatedAt"
                )
            }
        }
    }

    private fun request() = MockHttpServletRequest("GET", "/api/v1/study-recruitments").apply {
        serverName = "localhost"
        serverPort = 80
        servletPath = "/api/v1/study-recruitments"
    }

    private fun createDto(id: String) = StudyRecruitmentDto(
        id = id,
        externalId = "external-$id",
        type = StudyRecruitmentType.INFLEARN,
        title = "title-$id",
        content = "content-$id",
        url = "https://example.com/$id",
        writer = "writer-$id",
        createdAt = null,
        updatedAt = null
    )

    private fun responseDtoFieldNames(): List<String> {
        return Class.forName(
            "net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.in.web.dto.StudyRecruitmentResponse"
        ).declaredFields.map { it.name }
    }
}
