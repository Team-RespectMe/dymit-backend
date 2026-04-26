package net.noti_me.dymit.dymit_backend_api.units.adapter.study_recruitment

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.StudyRecruitmentServiceFacade
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.dto.QueryStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.usecases.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import org.bson.types.ObjectId

/**
 * StudyRecruitmentServiceFacade 단위 테스트입니다.
 *
 * 서비스 파사드가 조회 사이즈를 size + 1로 확장해 유즈케이스에 위임하는지 검증합니다.
 */
internal class StudyRecruitmentServiceFacadeTest : BehaviorSpec() {

    private val queryStudyRecruitmentUseCase = mockk<QueryStudyRecruitmentUseCase>()

    private val studyRecruitmentServiceFacade = StudyRecruitmentServiceFacade(
        queryStudyRecruitmentUseCase = queryStudyRecruitmentUseCase
    )

    init {
        Given("스터디 모집 목록 조회 Query가 주어지면") {
            val query = QueryStudyRecruitmentQuery(
                cursor = ObjectId.get().toHexString(),
                size = 20
            )
            val expected = listOf(createStudyRecruitment())
            every {
                queryStudyRecruitmentUseCase.queryStudyRecruitments(
                    QueryStudyRecruitmentQuery(cursor = query.cursor, size = 21)
                )
            } returns expected

            When("서비스 파사드에서 목록 조회를 수행하면") {
                val result = studyRecruitmentServiceFacade.getStudyRecruitments(query)

                Then("유즈케이스에 size + 1로 전달하고 결과를 반환한다") {
                    verify(exactly = 1) {
                        queryStudyRecruitmentUseCase.queryStudyRecruitments(
                            QueryStudyRecruitmentQuery(cursor = query.cursor, size = 21)
                        )
                    }
                    result shouldBe expected
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
