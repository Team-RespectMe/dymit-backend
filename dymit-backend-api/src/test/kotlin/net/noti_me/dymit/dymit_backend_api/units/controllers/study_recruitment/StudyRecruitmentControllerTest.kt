package net.noti_me.dymit.dymit_backend_api.units.controllers.study_recruitment

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.StudyRecruitmentServiceFacade
import net.noti_me.dymit.dymit_backend_api.adapter.study_recruitment.dto.QueryStudyRecruitmentQuery
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_recruitment.StudyRecruitmentController
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import org.bson.types.ObjectId

/**
 * StudyRecruitmentController 단위 테스트입니다.
 *
 * 컨트롤러가 요청 DTO를 Query DTO로 변환하고,
 * 서비스 결과를 응답 DTO와 ListResponse 형태로 변환하는지 검증합니다.
 */
internal class StudyRecruitmentControllerTest : BehaviorSpec() {

    private val studyRecruitmentServiceFacade = mockk<StudyRecruitmentServiceFacade>()

    private val controller = StudyRecruitmentController(studyRecruitmentServiceFacade)

    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "member",
        roles = listOf(MemberRole.ROLE_MEMBER)
    )

    init {
        Given("스터디 모집 목록 조회 요청이 주어지면") {
            val cursor: String? = null
            val size = 2
            val recruitments = listOf(
                createStudyRecruitment("1"),
                createStudyRecruitment("2")
            )
            every {
                studyRecruitmentServiceFacade.getStudyRecruitments(
                    QueryStudyRecruitmentQuery(cursor = cursor, size = size)
                )
            } returns recruitments

            When("컨트롤러에서 목록을 조회하면") {
                val result = controller.getStudyRecruitments(memberInfo, cursor, size)

                Then("서비스 파사드에 변환된 Query를 전달하고 응답 형식을 맞춰 반환한다") {
                    verify(exactly = 1) {
                        studyRecruitmentServiceFacade.getStudyRecruitments(
                            QueryStudyRecruitmentQuery(cursor = cursor, size = size)
                        )
                    }
                    result.count shouldBe 2L
                    result.items.size shouldBe 2
                    result.items[0].externalId shouldBe "external-1"
                    result.items[1].externalId shouldBe "external-2"
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }

    private fun createStudyRecruitment(suffix: String): StudyRecruitment {
        return StudyRecruitment(
            id = ObjectId.get(),
            externalId = "external-$suffix",
            type = "INFLEARN",
            title = "title-$suffix",
            content = "content-$suffix",
            url = "https://example.com/$suffix",
            writer = "writer-$suffix"
        )
    }
}
