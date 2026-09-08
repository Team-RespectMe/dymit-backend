package net.noti_me.dymit.dymit_backend_api.units.study_recruitment.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.CheckDymitStudyRecruitmentExistenceService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CheckDymitStudyRecruitmentExistenceCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.CheckDymitStudyRecruitmentExistencePort
import org.bson.types.ObjectId

internal class CheckDymitStudyRecruitmentExistenceServiceTest : BehaviorSpec() {

    private val existencePort = mockk<CheckDymitStudyRecruitmentExistencePort>()
    private val service = CheckDymitStudyRecruitmentExistenceService(existencePort)

    init {
        afterEach {
            clearAllMocks()
        }

        Given("그룹별 모집글 존재 여부 확인 서비스") {
            val firstGroupId = ObjectId.get()
            val secondGroupId = ObjectId.get()
            every { existencePort.existsActiveByGroupId(firstGroupId) } returns true
            every { existencePort.existsActiveByGroupId(secondGroupId) } returns false

            When("여러 그룹 식별자를 전달하면") {
                val result = service.execute(
                    CheckDymitStudyRecruitmentExistenceCommand(
                        groupIds = listOf(
                            firstGroupId.toHexString(),
                            secondGroupId.toHexString()
                        )
                    )
                )

                Then("요청 순서대로 각 그룹의 존재 여부를 반환한다") {
                    result.map { it.groupId } shouldBe listOf(
                        firstGroupId.toHexString(),
                        secondGroupId.toHexString()
                    )
                    result.map { it.exists } shouldBe listOf(true, false)
                    verify(exactly = 1) { existencePort.existsActiveByGroupId(firstGroupId) }
                    verify(exactly = 1) { existencePort.existsActiveByGroupId(secondGroupId) }
                }
            }

            Then("올바르지 않은 그룹 식별자는 BadRequestException으로 거부한다") {
                shouldThrow<BadRequestException> {
                    service.execute(
                        CheckDymitStudyRecruitmentExistenceCommand(
                            groupIds = listOf("invalid-group-id")
                        )
                    )
                }.message shouldBe "올바르지 않은 그룹 식별자입니다."
            }
        }
    }
}
