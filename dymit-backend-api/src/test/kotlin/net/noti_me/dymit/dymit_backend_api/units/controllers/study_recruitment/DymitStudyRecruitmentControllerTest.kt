package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.`in`.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.validation.Validation
import jakarta.validation.Validator
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.web.DymitStudyRecruitmentController
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CreateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.DeleteDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentListUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.UpdateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CreateDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DeleteDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentSummaryDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentListQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.GetDymitStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.UpdateDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.DymitStudyRecruitmentApi
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.CreateStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentSummaryResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.DymitStudyRecruitmentWriterResponse
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.web.dto.UpdateStudyRecruitmentRequest
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant
import java.time.LocalDateTime

internal class DymitStudyRecruitmentControllerTest : BehaviorSpec() {

    private val createUseCase = mockk<CreateDymitStudyRecruitmentUseCase>()
    private val getListUseCase = mockk<GetDymitStudyRecruitmentListUseCase>()
    private val getUseCase = mockk<GetDymitStudyRecruitmentUseCase>()
    private val updateUseCase = mockk<UpdateDymitStudyRecruitmentUseCase>()
    private val deleteUseCase = mockk<DeleteDymitStudyRecruitmentUseCase>(relaxed = true)
    private val controller = DymitStudyRecruitmentController(
        createUseCase = createUseCase,
        getListUseCase = getListUseCase,
        getUseCase = getUseCase,
        updateUseCase = updateUseCase,
        deleteUseCase = deleteUseCase
    )
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator
    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )

    init {
        afterEach {
            RequestContextHolder.resetRequestAttributes()
        }

        Given("생성 요청 DTO 검증") {
            Then("description 최대 길이를 넘기면 실패한다") {
                val request = createRequest(description = "a".repeat(201))
                validator.validate(request).map { it.message } shouldContain "size must be between 0 and 200"
            }

            Then("toCommand는 정확한 입력 타입으로 변환한다") {
                val request = createRequest(tags = listOf("kotlin"))
                request.toCommand() shouldBe CreateDymitStudyRecruitmentCommand(
                    groupId = request.groupId,
                    description = request.description,
                    purpose = request.purpose,
                    targetMember = request.targetMember,
                    studyFormat = request.studyFormat,
                    contact = request.contact,
                    recruitmentStart = request.recruitmentStart,
                    recruitmentEnd = request.recruitmentEnd,
                    tags = listOf("kotlin")
                )
            }
        }

        Given("수정 요청 DTO 검증") {
            Then("purpose 최대 길이를 넘기면 실패한다") {
                val request = updateRequest(purpose = "a".repeat(51))
                validator.validate(request).map { it.message } shouldContain "size must be between 0 and 50"
            }

            Then("toCommand는 recruitmentId를 포함한 정확한 입력 타입으로 변환한다") {
                val request = updateRequest(tags = listOf("backend"))
                request.toCommand("recruitment-id") shouldBe UpdateDymitStudyRecruitmentCommand(
                    recruitmentId = "recruitment-id",
                    description = request.description,
                    purpose = request.purpose,
                    targetMember = request.targetMember,
                    studyFormat = request.studyFormat,
                    contact = request.contact,
                    recruitmentStart = request.recruitmentStart,
                    recruitmentEnd = request.recruitmentEnd,
                    status = request.status,
                    tags = listOf("backend")
                )
            }
        }

        Given("v2 생성 컨트롤러") {
            val request = createRequest(tags = listOf("kotlin"))
            val dto = createDto(id = "new-id")
            every { createUseCase.execute(memberInfo, any()) } returns dto

            When("createStudyRecruitment를 호출하면") {
                val command = slot<CreateDymitStudyRecruitmentCommand>()
                every { createUseCase.execute(memberInfo, capture(command)) } returns dto

                val response = controller.createStudyRecruitment(memberInfo, request)

                Then("생성 유즈케이스에 명령을 전달하고 응답으로 변환한다") {
                    verify(exactly = 1) { createUseCase.execute(memberInfo, any()) }
                    command.captured.groupId shouldBe request.groupId
                    command.captured.tags shouldBe listOf("kotlin")
                    response.id shouldBe "new-id"
                    response.type shouldBe StudyRecruitmentType.DYMIT
                    response.writer.id shouldBe memberInfo.memberId
                    response.writer.name shouldBe memberInfo.nickname
                    response.writer.profileImageUrl shouldBe "https://example.com/profile-thumb.png"
                }
            }
        }

        Given("v2 목록 조회 컨트롤러") {
            RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request("/api/v2/study-recruitments")))
            val first = createSummaryDto(id = "1")
            val second = createSummaryDto(id = "2")
            val third = createSummaryDto(id = "3")
            every { getListUseCase.execute(GetDymitStudyRecruitmentListQuery(cursor = null, size = 2)) } returns listOf(first, second, third)

            When("getStudyRecruitmentList를 호출하면") {
                val response = controller.getStudyRecruitmentList(null, 2)

                Then("summary 응답만 반환하고 next 링크와 size를 구성한다") {
                    verify(exactly = 1) { getListUseCase.execute(GetDymitStudyRecruitmentListQuery(cursor = null, size = 2)) }
                    response.count shouldBe 2L
                    response.items.map(DymitStudyRecruitmentSummaryResponse::id) shouldBe listOf("1", "2")
                    response.items.map { it.title } shouldBe listOf("테스트 그룹", "테스트 그룹")
                    response.items.map { it.tags } shouldBe listOf(listOf("kotlin"), listOf("kotlin"))
                    response.items.map { it.type } shouldBe listOf(StudyRecruitmentType.DYMIT, StudyRecruitmentType.DYMIT)
                    response.items.map { it.status } shouldBe listOf(
                        DymitStudyRecruitmentStatus.RECRUITING,
                        DymitStudyRecruitmentStatus.RECRUITING
                    )
                    response._links["next"]?.href?.contains("cursor=2") shouldBe true
                    response._links["next"]?.href?.contains("size=2") shouldBe true
                }
            }
        }

        Given("v2 단건 조회 컨트롤러") {
            every { getUseCase.execute(GetDymitStudyRecruitmentQuery("recruitment-id")) } returns createDto(id = "recruitment-id")

            When("getStudyRecruitment를 호출하면") {
                val response = controller.getStudyRecruitment("recruitment-id")

                Then("GetDymitStudyRecruitmentQuery를 사용한다") {
                    verify(exactly = 1) { getUseCase.execute(GetDymitStudyRecruitmentQuery("recruitment-id")) }
                    response.id shouldBe "recruitment-id"
                    response.writer.id shouldBe memberInfo.memberId
                    response.writer.name shouldBe memberInfo.nickname
                    response.writer.profileImageUrl shouldBe "https://example.com/profile-thumb.png"
                }
            }
        }

        Given("v2 수정 컨트롤러") {
            val request = updateRequest()
            val command = slot<UpdateDymitStudyRecruitmentCommand>()
            every { updateUseCase.execute(memberInfo, capture(command)) } returns createDto(id = "updated-id")

            When("updateStudyRecruitment를 호출하면") {
                val response = controller.updateStudyRecruitment(memberInfo, "updated-id", request)

                Then("수정 유즈케이스에 recruitmentId를 포함한 명령을 전달한다") {
                    verify(exactly = 1) { updateUseCase.execute(memberInfo, any()) }
                    command.captured.recruitmentId shouldBe "updated-id"
                    command.captured.status shouldBe DymitStudyRecruitmentStatus.DONE
                    response.id shouldBe "updated-id"
                }
            }
        }

        Given("v2 삭제 컨트롤러") {
            val command = slot<DeleteDymitStudyRecruitmentCommand>()
            every { deleteUseCase.execute(memberInfo, capture(command)) } returns Unit

            When("deleteStudyRecruitment를 호출하면") {
                controller.deleteStudyRecruitment(memberInfo, "delete-id")

                Then("삭제 명령만 전달한다") {
                    verify(exactly = 1) { deleteUseCase.execute(memberInfo, any()) }
                    command.captured shouldBe DeleteDymitStudyRecruitmentCommand("delete-id")
                }
            }
        }

        Given("패키지 구조와 입력 타입 규칙") {
            Then("API 인터페이스는 application port 아래에 있다") {
                DymitStudyRecruitmentApi::class.java.`package`.name shouldBe
                    "net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.in.web"
            }

            Then("컨트롤러는 adapter 패키지에 있다") {
                DymitStudyRecruitmentController::class.java.`package`.name shouldBe
                    "net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.in.web"
            }

            Then("Get/List 유즈케이스는 Command가 아닌 Query 타입을 사용한다") {
                GetDymitStudyRecruitmentUseCase::class.java.methods.single { it.name == "execute" }
                    .parameterTypes.single().simpleName shouldBe "GetDymitStudyRecruitmentQuery"
                GetDymitStudyRecruitmentListUseCase::class.java.methods.single { it.name == "execute" }
                    .parameterTypes.single().simpleName shouldBe "GetDymitStudyRecruitmentListQuery"
                DymitStudyRecruitmentResponse::class.java.declaredFields.map { it.name } shouldContainAll
                    listOf("writer", "groupId", "type", "tags")
                DymitStudyRecruitmentResponse::class.java.declaredFields.map { it.name } shouldNotContain "writerId"
                DymitStudyRecruitmentResponse::class.java.declaredFields.map { it.name } shouldNotContain "writerNickname"
                DymitStudyRecruitmentWriterResponse::class.java.declaredFields.map { it.name } shouldContainAll
                    listOf("id", "name", "profileImageUrl")
                CreateStudyRecruitmentRequest::class.java.declaredFields.map { it.name } shouldNotContain "title"
            }

            Then("v2 목록 응답 DTO는 summary 필드만 가진다") {
                DymitStudyRecruitmentSummaryResponse::class.java.declaredFields.map { it.name } shouldContainAll
                    listOf("id", "createdAt", "title", "tags", "type", "status")
                DymitStudyRecruitmentSummaryResponse::class.java.declaredFields.map { it.name } shouldNotContain "writer"
                DymitStudyRecruitmentSummaryResponse::class.java.declaredFields.map { it.name } shouldNotContain "groupId"
                DymitStudyRecruitmentSummaryResponse::class.java.declaredFields.map { it.name } shouldNotContain "description"
                DymitStudyRecruitmentSummaryResponse::class.java.declaredFields.map { it.name } shouldNotContain "contact"
            }
        }
    }

    private fun request(path: String) = MockHttpServletRequest("GET", path).apply {
        serverName = "localhost"
        serverPort = 80
        servletPath = path
    }

    private fun createRequest(
        description: String = "소개",
        purpose: String = "목적",
        tags: List<String> = emptyList()
    ) = CreateStudyRecruitmentRequest(
        groupId = ObjectId.get().toHexString(),
        description = description,
        purpose = purpose,
        targetMember = "백엔드",
        studyFormat = "온라인",
        contact = "https://example.com/contact",
        recruitmentStart = Instant.parse("2026-08-17T00:00:00Z"),
        recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z"),
        tags = tags
    )

    private fun updateRequest(
        purpose: String = "목적",
        tags: List<String> = emptyList()
    ) = UpdateStudyRecruitmentRequest(
        description = "소개",
        purpose = purpose,
        targetMember = "백엔드",
        studyFormat = "온라인",
        contact = "https://example.com/contact",
        recruitmentStart = Instant.parse("2026-08-17T00:00:00Z"),
        recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z"),
        status = DymitStudyRecruitmentStatus.DONE,
        tags = tags
    )

    private fun createDto(id: String) = DymitStudyRecruitmentDto(
        id = id,
        writerId = memberInfo.memberId,
        writerNickname = memberInfo.nickname,
        writerProfileImageUrl = "https://example.com/profile-thumb.png",
        groupId = ObjectId.get().toHexString(),
        type = StudyRecruitmentType.DYMIT,
        title = "테스트 그룹",
        description = "소개",
        purpose = "목적",
        recruitmentStatus = DymitStudyRecruitmentStatus.RECRUITING,
        recruitmentStart = Instant.parse("2026-08-17T00:00:00Z"),
        recruitmentEnd = Instant.parse("2026-08-24T00:00:00Z"),
        targetMember = "백엔드",
        studyFormat = "온라인",
        contact = "https://example.com/contact",
        tags = listOf("kotlin"),
        createdAt = LocalDateTime.of(2026, 8, 17, 9, 0),
        updatedAt = LocalDateTime.of(2026, 8, 17, 9, 0)
    )

    private fun createSummaryDto(id: String) = DymitStudyRecruitmentSummaryDto(
        id = id,
        createdAt = LocalDateTime.of(2026, 8, 17, 9, 0),
        title = "테스트 그룹",
        tags = listOf("kotlin"),
        type = StudyRecruitmentType.DYMIT,
        status = DymitStudyRecruitmentStatus.RECRUITING
    )
}
