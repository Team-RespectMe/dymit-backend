package net.noti_me.dymit.dymit_backend_api.units.controllers.study_recruitment

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.web.DymitStudyRecruitmentController
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.BumpStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CheckDymitStudyRecruitmentExistenceUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CreateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.DeleteDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentListUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.GetDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.QueryStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.UpdateDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CheckDymitStudyRecruitmentExistenceCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentExistenceDto

internal class DymitStudyRecruitmentExistenceControllerTest : BehaviorSpec() {

    private val createUseCase = mockk<CreateDymitStudyRecruitmentUseCase>()
    private val getListUseCase = mockk<GetDymitStudyRecruitmentListUseCase>()
    private val queryExternalUseCase = mockk<QueryStudyRecruitmentUseCase>()
    private val getUseCase = mockk<GetDymitStudyRecruitmentUseCase>()
    private val updateUseCase = mockk<UpdateDymitStudyRecruitmentUseCase>()
    private val deleteUseCase = mockk<DeleteDymitStudyRecruitmentUseCase>()
    private val bumpUseCase = mockk<BumpStudyRecruitmentUseCase>()
    private val existenceUseCase = mockk<CheckDymitStudyRecruitmentExistenceUseCase>()
    private val controller = DymitStudyRecruitmentController(
        createUseCase = createUseCase,
        getListUseCase = getListUseCase,
        queryExternalUseCase = queryExternalUseCase,
        getUseCase = getUseCase,
        updateUseCase = updateUseCase,
        deleteUseCase = deleteUseCase,
        bumpUseCase = bumpUseCase,
        checkExistenceUseCase = existenceUseCase
    )

    init {
        Given("그룹별 모집글 존재 여부 조회 요청") {
            val groupIds = listOf("000000000000000000000001", "000000000000000000000002")
            val command = slot<CheckDymitStudyRecruitmentExistenceCommand>()
            every { existenceUseCase.execute(capture(command)) } returns listOf(
                DymitStudyRecruitmentExistenceDto(groupIds[0], true),
                DymitStudyRecruitmentExistenceDto(groupIds[1], false)
            )

            When("컨트롤러의 존재 여부 API를 호출하면") {
                val result = controller.getStudyRecruitmentExistence(groupIds)

                Then("그룹 ID 목록을 유즈케이스에 전달하고 결과를 응답한다") {
                    command.captured shouldBe CheckDymitStudyRecruitmentExistenceCommand(groupIds)
                    result.map { it.groupId } shouldBe groupIds
                    result.map { it.exists } shouldBe listOf(true, false)
                    verify(exactly = 1) { existenceUseCase.execute(any()) }
                }
            }
        }
    }
}
