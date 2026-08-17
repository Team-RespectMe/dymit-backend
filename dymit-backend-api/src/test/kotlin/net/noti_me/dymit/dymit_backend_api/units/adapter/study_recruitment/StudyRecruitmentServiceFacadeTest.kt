package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.QueryStudyRecruitmentService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.QueryStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.LoadStudyRecruitmentPort
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.dto.StudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId

/** Query study recruitment service unit tests. */
internal class StudyRecruitmentServiceFacadeTest : BehaviorSpec() {

    private val loadStudyRecruitmentPort = mockk<LoadStudyRecruitmentPort>()
    private val service = QueryStudyRecruitmentService(loadStudyRecruitmentPort)

    init {
        Given("a command with a valid cursor") {
            val cursor = ObjectId.get()
            val persistenceDto = createPersistenceDto("recruitment-id")
            every { loadStudyRecruitmentPort.findByCursorOrderByIdDesc(cursor, 21) } returns listOf(persistenceDto)

            When("the use case executes") {
                val result = service.execute(QueryStudyRecruitmentCommand(cursor.toHexString(), 20))

                Then("it requests size plus one and maps output DTOs to input DTOs") {
                    verify(exactly = 1) { loadStudyRecruitmentPort.findByCursorOrderByIdDesc(cursor, 21) }
                    result.single().id shouldBe "recruitment-id"
                    result.single().externalId shouldBe persistenceDto.externalId
                }
            }
        }

        Given("a command without a cursor") {
            every { loadStudyRecruitmentPort.findByCursorOrderByIdDesc(null, 6) } returns emptyList()

            When("the use case executes") {
                service.execute(QueryStudyRecruitmentCommand(size = 5))

                Then("it delegates a null cursor to preserve first-page behavior") {
                    verify(exactly = 1) { loadStudyRecruitmentPort.findByCursorOrderByIdDesc(null, 6) }
                }
            }
        }

        Given("an invalid cursor") {
            When("the use case executes") {
                Then("it rejects the cursor before querying the output port") {
                    clearMocks(loadStudyRecruitmentPort)
                    shouldThrow<IllegalArgumentException> {
                        service.execute(QueryStudyRecruitmentCommand(cursor = "invalid-object-id"))
                    }
                    verify(exactly = 0) { loadStudyRecruitmentPort.findByCursorOrderByIdDesc(any(), any()) }
                }
            }
        }
    }

    private fun createPersistenceDto(id: String) = StudyRecruitmentPersistenceDto(
        id = id,
        externalId = "external-$id",
        type = StudyRecruitmentType.INFLEARN,
        title = "title",
        content = "content",
        url = "https://example.com/$id",
        writer = "writer",
        createdAt = null,
        updatedAt = null
    )
}
