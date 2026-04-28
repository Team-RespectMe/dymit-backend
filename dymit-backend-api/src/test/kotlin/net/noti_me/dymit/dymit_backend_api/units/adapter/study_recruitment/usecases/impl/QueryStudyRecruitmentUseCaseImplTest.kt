package net.noti_me.dymit.dymit_backend_api.units.adapter.study_recruitment.usecases.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.dto.QueryStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.usecases.impl.QueryStudyRecruitmentUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_recruitment.StudyRecruitmentRepository
import org.bson.types.ObjectId

/**
 * QueryStudyRecruitmentUseCaseImpl 단위 테스트입니다.
 *
 * cursor 문자열 변환과 repository 위임 동작을 검증합니다.
 */
internal class QueryStudyRecruitmentUseCaseImplTest : BehaviorSpec() {

    private val studyRecruitmentRepository = mockk<StudyRecruitmentRepository>()

    private val queryStudyRecruitmentUseCase = QueryStudyRecruitmentUseCaseImpl(
        studyRecruitmentRepository = studyRecruitmentRepository
    )

    init {
        Given("cursor 없이 목록 조회를 요청하면") {
            val query = QueryStudyRecruitmentQuery(
                cursor = null,
                size = 10
            )
            val expected = listOf(createStudyRecruitment())
            every {
                studyRecruitmentRepository.findByCursorOrderByIdDesc(null, 10)
            } returns expected

            When("유즈케이스를 실행하면") {
                val result = queryStudyRecruitmentUseCase.queryStudyRecruitments(query)

                Then("repository에 null cursor로 위임한다") {
                    verify(exactly = 1) {
                        studyRecruitmentRepository.findByCursorOrderByIdDesc(null, 10)
                    }
                    result shouldBe expected
                }
            }
        }

        Given("유효한 cursor와 함께 목록 조회를 요청하면") {
            val cursor = ObjectId.get().toHexString()
            val query = QueryStudyRecruitmentQuery(
                cursor = cursor,
                size = 5
            )
            val expected = listOf(createStudyRecruitment())
            every {
                studyRecruitmentRepository.findByCursorOrderByIdDesc(ObjectId(cursor), 5)
            } returns expected

            When("유즈케이스를 실행하면") {
                val result = queryStudyRecruitmentUseCase.queryStudyRecruitments(query)

                Then("cursor를 ObjectId로 변환해 repository에 위임한다") {
                    verify(exactly = 1) {
                        studyRecruitmentRepository.findByCursorOrderByIdDesc(ObjectId(cursor), 5)
                    }
                    result shouldBe expected
                }
            }
        }

        Given("유효하지 않은 cursor가 주어지면") {
            val query = QueryStudyRecruitmentQuery(
                cursor = "invalid-object-id",
                size = 5
            )

            When("유즈케이스를 실행하면") {
                Then("IllegalArgumentException이 발생한다") {
                    shouldThrow<IllegalArgumentException> {
                        queryStudyRecruitmentUseCase.queryStudyRecruitments(query)
                    }
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }

    private fun createStudyRecruitment(): StudyRecruitment {
        return StudyRecruitment(
            id = ObjectId.get(),
            externalId = "external-id",
            type = "INFLEARN",
            title = "title",
            content = "content",
            url = "https://example.com/recruitment",
            writer = "writer"
        )
    }
}
