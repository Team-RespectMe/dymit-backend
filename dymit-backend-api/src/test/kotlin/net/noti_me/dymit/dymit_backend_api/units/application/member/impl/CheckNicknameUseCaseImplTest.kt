package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.member.impl.CheckNicknameUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort

internal class CheckNicknameUseCaseImplTest(): BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val checkNicknameUseCase = CheckNicknameUseCaseImpl(
        loadMemberPort = loadMemberPort
    )

    init {

        Given("닉네임이 주어진다.") {
            val nickname = "nickname"
            When("중복되는 닉네임이라면 ") {
                every { loadMemberPort.existsByNickname(nickname) } returns true
                Then("ConflictException 예외가 발생한다.") {
                    shouldThrow<ConflictException> {
                        checkNicknameUseCase.isNicknameAvailable(nickname)
                    }
                }
            }

            When("중복되지 않는 닉네임이라면 ") {
                every { loadMemberPort.existsByNickname(nickname) } returns false
                Then("예외가 발생하지 않는다.") {
                    checkNicknameUseCase.isNicknameAvailable(nickname)
                }
            }
        }

        afterEach { clearAllMocks() }
    }
}