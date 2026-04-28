package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.member.impl.ManageDeviceTokenUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId

internal class ManageDeviceTokenUseCaseImplTest()
: BehaviorSpec() {

    private val memberId = ObjectId.get()

    private val otherId = ObjectId.get()

    private var member = createMemberEntity(id = memberId)

    private var otherMember = createMemberEntity()

    private var memberInfo = createMemberInfo(member)

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val manageDeviceTokenUseCaseImpl = ManageDeviceTokenUseCaseImpl (
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort
    )


    init {

        beforeContainer {
            member = createMemberEntity(id = memberId)
            otherMember = createMemberEntity(id = otherId)
            memberInfo = createMemberInfo(member)
            otherMember.addDeviceToken(DeviceToken(token = "DeviceToken", isActive = true))
        }

        afterEach {
//            clearAllMocks()
        }

        Given("등록할 디바이스 토큰이 주어진다.") {
            val token = "DeviceToken"
            When("회원이 존재하지 않으면") {
                every { loadMemberPort.loadById(memberInfo.memberId)} returns null
                every { loadMemberPort.loadByDeviceToken(token) } returns emptyList()
                Then("NotFoundException 예외가 발생한다.") {
                    shouldThrow<NotFoundException> {
                        manageDeviceTokenUseCaseImpl.registerDeviceToken(
                            member = memberInfo,
                            deviceToken = token
                        )
                    }
                }
            }

            When("다른 사용자가 해당 디바이스 토큰을 이미 등록한 상태이면") {
                every { loadMemberPort.loadByDeviceToken(token) } returns listOf(otherMember)
                every { loadMemberPort.loadById(memberInfo.memberId)} returns member
                every { saveMemberPort.update( any() ) } answers { firstArg() }
                Then("기존 사용자의 디바이스 토큰이 비활성화되고, 현재 사용자의 디바이스 토큰이 등록된다.") {
                    manageDeviceTokenUseCaseImpl.registerDeviceToken(memberInfo, token)
                    otherMember.deviceTokens.isEmpty() shouldBe true
                    member.deviceTokens.size shouldBe 1
                }
            }

            When("아직 아무도 해당 디바이스 토큰을 등록하지 않은 상태이면") {
                every { loadMemberPort.loadByDeviceToken(token) } returns emptyList()
                every { loadMemberPort.loadById(memberInfo.memberId)} returns member
                every { saveMemberPort.update( any() ) } answers { firstArg() }
                Then("현재 사용자의 디바이스 토큰이 등록된다.") {
                    manageDeviceTokenUseCaseImpl.registerDeviceToken(memberInfo, token)
                    member.deviceTokens.size shouldBe 1
                }
            }
        }


        Given("해제할 디바이스 토큰이 주어진다.") {
            val token = "DeviceToken"
            When("회원이 존재하지 않으면") {
                every { loadMemberPort.loadById(memberInfo.memberId) } returns null
                Then("NotFoundException 예외가 발생한다.") {
                    shouldThrow<NotFoundException> {
                        manageDeviceTokenUseCaseImpl.unregisterDeviceToken(
                            member = memberInfo,
                            deviceToken = token
                        )
                    }
                }
            }

            When("회원이 존재하면") {
                member.addDeviceToken(DeviceToken(token = token, isActive = true))
                every { loadMemberPort.loadById(memberInfo.memberId) } returns member
                every { saveMemberPort.update(any()) } answers { firstArg() }
                Then("해당 디바이스 토큰이 회원에서 제거된다.") {
                    manageDeviceTokenUseCaseImpl.unregisterDeviceToken(
                        member = memberInfo,
                        deviceToken = token
                    )
                    member.deviceTokens.isEmpty() shouldBe true
                }
            }
        }
    }
}