package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.member.dto.UpdateInterestsCommand
import net.noti_me.dymit.dymit_backend_api.application.member.impl.UpdateInterestsUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId

internal class UpdateInterestsUseCaseImplTest() : BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val updateInterestsUseCase = UpdateInterestsUseCaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort
    )

    private val memberId = ObjectId.get()

    private var member = createMemberEntity(memberId)

    private var memberInfo = createMemberInfo(member)

    init {

        Given("회원 관심사 수정 명령이 주어진다.") {
            val command = UpdateInterestsCommand(interests = listOf("Kotlin", "Spring"))
            When("멤버가 존재하지 않으면") {
                every { loadMemberPort.loadById(memberId.toHexString()) } returns null
                Then("NotFoundException이 발생한다.") {
                    shouldThrow<NotFoundException> {
                        updateInterestsUseCase.updateInterests(
                            loginMember = memberInfo,
                            command = command
                        )
                    }
                }
            }

            When("멤버가 존재하면") {
                every { loadMemberPort.loadById(memberId.toHexString()) } returns member
                every { saveMemberPort.update(any()) } returns member

                Then("회원의 관심사가 수정된다.") {
                    val result = updateInterestsUseCase.updateInterests(
                        loginMember = memberInfo,
                        command = command
                    )
                    result.interests.size shouldBe 2
                    result.interests shouldBe command.interests.toSet()
                }
            }
        }
    }
}