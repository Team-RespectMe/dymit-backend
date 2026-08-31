package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.adapter.`in`.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.web.DymitStudyRecruitmentController
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.BumpStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CreateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.DeleteDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentListUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.UpdateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.BumpStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDateTime

internal class DymitStudyRecruitmentBumpControllerTest : BehaviorSpec() {

    private val createUseCase = mockk<CreateDymitStudyRecruitmentUseCase>()
    private val getListUseCase = mockk<GetDymitStudyRecruitmentListUseCase>()
    private val queryExternalUseCase = mockk<QueryStudyRecruitmentUseCase>()
    private val getUseCase = mockk<GetDymitStudyRecruitmentUseCase>()
    private val updateUseCase = mockk<UpdateDymitStudyRecruitmentUseCase>()
    private val deleteUseCase = mockk<DeleteDymitStudyRecruitmentUseCase>()
    private val bumpUseCase = mockk<BumpStudyRecruitmentUseCase>()
    private val controller = DymitStudyRecruitmentController(
        createUseCase = createUseCase,
        getListUseCase = getListUseCase,
        queryExternalUseCase = queryExternalUseCase,
        getUseCase = getUseCase,
        updateUseCase = updateUseCase,
        deleteUseCase = deleteUseCase,
        bumpUseCase = bumpUseCase
    )
    private val memberInfo = MemberInfo(
        memberId = ObjectId.get().toHexString(),
        nickname = "tester",
        roles = listOf(MemberRole.ROLE_MEMBER.name)
    )

    init {
        Given("v2 끌어올리기 컨트롤러") {
            val command = slot<BumpStudyRecruitmentCommand>()
            every { bumpUseCase.execute(memberInfo, capture(command)) } returns createDto(id = "bumped-id")

            When("bumpStudyRecruitment를 호출하면") {
                val response = controller.bumpStudyRecruitment(memberInfo, "bumped-id")

                Then("인증된 회원 정보와 recruitmentId를 유즈케이스에 전달하고 단건 응답으로 변환한다") {
                    verify(exactly = 1) { bumpUseCase.execute(memberInfo, any()) }
                    command.captured shouldBe BumpStudyRecruitmentCommand("bumped-id")
                    response.id shouldBe "bumped-id"
                    response.bumpCount shouldBe 0
                    response.writer.id shouldBe memberInfo.memberId
                }
            }
        }
    }

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
        contact = Contact(
            url = "https://example.com/contact",
            title = "오픈채팅"
        ),
        tags = listOf("kotlin"),
        createdAt = LocalDateTime.of(2026, 8, 17, 9, 0),
        updatedAt = LocalDateTime.of(2026, 8, 17, 9, 0)
    )
}
