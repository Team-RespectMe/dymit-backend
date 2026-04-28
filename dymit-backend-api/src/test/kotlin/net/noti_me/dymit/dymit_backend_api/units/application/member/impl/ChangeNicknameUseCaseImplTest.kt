package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.member.dto.UpdateNicknameCommand
import net.noti_me.dymit.dymit_backend_api.application.member.impl.ChangeNicknameUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo

internal class ChangeNicknameUseCaseImplTest: BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val updateNicknameUsecase = ChangeNicknameUseCaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort
    )

    private var member: Member = createMemberEntity()

    init {
        beforeEach {
            member = createMemberEntity()
            every { loadMemberPort.loadById( member.identifier ) } returns member
            every { saveMemberPort.update(member ) } returns  member
        }

        given("중복되지 않는 길이 1자 미만의 변경할 닉네임이 주어진다.") {
            val newNickname = ""
            every { loadMemberPort.existsByNickname(newNickname) } returns false
            `when`("자기 자신의 닉네임을 변경하면") {

                then("IllegalArgumentException이 발생한다.") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        updateNicknameUsecase.updateNickname(
                            loginMember = createMemberInfo(member),
                            memberId = member.identifier,
                            command = UpdateNicknameCommand(nickname = newNickname)
                        )
                    }
                }
            }
        }

        given("중복되지 않는 길이 20자 초과 닉네임이 주어진다") {
            val newNickname = "a".repeat(21)
            every { loadMemberPort.existsByNickname(newNickname) } returns false
            `when`("자기 자신의 닉네임을 변경하면") {

                then("IllegalArgumentException이 발생한다.") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        updateNicknameUsecase.updateNickname(
                            loginMember = createMemberInfo(member),
                            memberId = member.identifier,
                            command = UpdateNicknameCommand(nickname = newNickname)
                        )
                    }
                }
            }
        }

        given("중복되지 않는 길이 1자 이상 20자 이하의 변경할 닉네임이 주어진다.") {
            val newNickname = "newNickname"

            `when`("자기 자신의 닉네임을 변경하면") {
                every { loadMemberPort.existsByNickname(newNickname) } returns false

                then("변경된 멤버 엔티티가 반환된다.") {
                    val updatedMember = updateNicknameUsecase.updateNickname(
                        loginMember = createMemberInfo(member),
                        memberId = member.identifier,
                        command = UpdateNicknameCommand(nickname = newNickname)
                    )
                    updatedMember.nickname shouldBe newNickname
                }
            }

            `when`("타인의 닉네임을 변경하면") {
                val other = createMemberEntity(nickname = "otherNickname")
                every { loadMemberPort.loadById(other.identifier) } returns other
                every { loadMemberPort.existsByNickname(newNickname) } returns false

                then("ForbiddenException이 발생한다.") {
                    val exception = shouldThrow<ForbiddenException> {
                        updateNicknameUsecase.updateNickname(
                            loginMember = createMemberInfo(member),
                            memberId = other.identifier,
                            command = UpdateNicknameCommand(nickname = newNickname)
                        )
                    }
                }
            }
        }

        given("중복되지 않는 정확히 1자인 닉네임이 주어진다.") {
            val newNickname = "a"
            every { loadMemberPort.existsByNickname(newNickname) } returns false

            `when`("자기 자신의 닉네임을 변경하면") {
                then("변경된 멤버 엔티티가 반환된다.") {
                    val updatedMember = updateNicknameUsecase.updateNickname(
                        loginMember = createMemberInfo(member),
                        memberId = member.identifier,
                        command = UpdateNicknameCommand(nickname = newNickname)
                    )
                    updatedMember.nickname shouldBe newNickname
                }
            }
        }

        given("중복되지 않는 정확히 20자인 닉네임이 주어진다.") {
            val newNickname = "a".repeat(20)
            every { loadMemberPort.existsByNickname(newNickname) } returns false

            `when`("자기 자신의 닉네임을 변경하면") {
                then("변경된 멤버 엔티티가 반환된다.") {
                    val updatedMember = updateNicknameUsecase.updateNickname(
                        loginMember = createMemberInfo(member),
                        memberId = member.identifier,
                        command = UpdateNicknameCommand(nickname = newNickname)
                    )
                    updatedMember.nickname shouldBe newNickname
                }
            }
        }

        given("중복되는 닉네임이 주어진다.") {
            val newNickname = "existingNickname"
            every { loadMemberPort.existsByNickname(newNickname) } returns true
            `when`("자기 자신의 닉네임을 변경하면") {
                then("ConflictException이 발생한다.") {
                    val exception = shouldThrow<ConflictException> {
                        updateNicknameUsecase.updateNickname(
                            loginMember = createMemberInfo(member),
                            memberId = member.identifier,
                            command = UpdateNicknameCommand(nickname = newNickname)
                        )
                    }
                }
            }
        }

        afterEach {
            clearAllMocks()
        }
    }

}