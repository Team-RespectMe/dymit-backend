package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.application.member.impl.MemberDeviceTokenUsecaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.DeviceToken
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.bson.types.ObjectId

/**
 * MemberDeviceTokenUsecaseImpl 테스트 클래스
 * 디바이스 토큰 등록/해제 기능에 대한 모든 시나리오를 테스트한다.
 */
class MemberDeviceTokenUsecaseImplTest : BehaviorSpec({

    // Mock 객체들
    val loadMemberPort = mockk<LoadMemberPort>(relaxed = true)
    val saveMemberPort = mockk<SaveMemberPort>(relaxed = true)

    // 테스트 대상
    val memberDeviceTokenUsecase = MemberDeviceTokenUsecaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort
    )

    // 테스트용 상수들
    val testDeviceToken = "test-device-token-123"
    val testMemberId = ObjectId.get().toHexString()
    val anotherMemberId = ObjectId.get().toHexString()

    beforeEach {
        clearAllMocks()
    }

    Given("디바이스 토큰을 등록하려는 상황에서") {

        When("정상적인 멤버가 새로운 디바이스 토큰을 등록할 때") {
            Then("디바이스 토큰이 성공적으로 등록된다") {
                // Given
                val testMember = Member(
                    id = ObjectId(testMemberId),
                    nickname = "testUser"
                )
                val testMemberInfo = MemberInfo(
                    memberId = testMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                every { loadMemberPort.loadByDeviceToken(testDeviceToken) } returns null
                every { loadMemberPort.loadById(testMemberId) } returns testMember
                every { saveMemberPort.update(any()) } returns testMember

                // When
                memberDeviceTokenUsecase.registerDeviceToken(testMemberInfo, testDeviceToken)

                // Then
                verify(exactly = 1) { loadMemberPort.loadByDeviceToken(testDeviceToken) }
                verify(exactly = 1) { loadMemberPort.loadById(testMemberId) }
                verify(exactly = 1) { saveMemberPort.update(testMember) }

                // 멤버에 토큰이 추가되었는지 확인
                testMember.deviceTokens.size shouldBe 1
                testMember.deviceTokens.first().token shouldBe testDeviceToken
                testMember.deviceTokens.first().isActive shouldBe true
            }
        }

        When("다른 멤버가 이미 사용중인 디바이스 토큰을 등록할 때") {
            Then("기존 소유자의 토큰을 업데이트하고 새로운 멤버에게 토큰을 등록한다") {
                // Given
                val testMember = Member(
                    id = ObjectId(testMemberId),
                    nickname = "testUser"
                )
                val testMemberInfo = MemberInfo(
                    memberId = testMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )
                val anotherMember = Member(
                    id = ObjectId(anotherMemberId),
                    nickname = "anotherUser"
                )

                // 다른 멤버가 이미 해당 토큰을 가지고 있는 상황
                anotherMember.addDeviceToken(DeviceToken(token = testDeviceToken, isActive = true))

                every { loadMemberPort.loadByDeviceToken(testDeviceToken) } returns anotherMember
                every { loadMemberPort.loadById(testMemberId) } returns testMember
                every { saveMemberPort.update(any()) } returns mockk()

                // When
                memberDeviceTokenUsecase.registerDeviceToken(testMemberInfo, testDeviceToken)

                // Then
                verify(exactly = 1) { loadMemberPort.loadByDeviceToken(testDeviceToken) }
                verify(exactly = 1) { loadMemberPort.loadById(testMemberId) }
                verify(exactly = 2) { saveMemberPort.update(any()) }

                // 새로운 멤버에게 토큰이 추가되었는지 확인
                testMember.deviceTokens.size shouldBe 1
                testMember.deviceTokens.first().token shouldBe testDeviceToken
            }
        }

        When("존재하지 않는 멤버가 디바이스 토큰을 등록하려고 할 때") {
            Then("NotFoundException이 발생한다") {
                // Given
                val testMemberInfo = MemberInfo(
                    memberId = testMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                every { loadMemberPort.loadByDeviceToken(testDeviceToken) } returns null
                every { loadMemberPort.loadById(testMemberId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    memberDeviceTokenUsecase.registerDeviceToken(testMemberInfo, testDeviceToken)
                }

                exception.message shouldBe "존재하지 않는 멤버입니다."

                verify(exactly = 1) { loadMemberPort.loadByDeviceToken(testDeviceToken) }
                verify(exactly = 1) { loadMemberPort.loadById(testMemberId) }
                verify(exactly = 0) { saveMemberPort.update(any()) }
            }
        }
    }

    Given("디바이스 토큰을 해제하려는 상황에서") {

        When("정상적인 멤버가 보유한 디바이스 토큰을 해제할 때") {
            Then("디바이스 토큰이 성공적으로 해제된다") {
                // Given
                val testMember = Member(
                    id = ObjectId(testMemberId),
                    nickname = "testUser"
                )
                val testMemberInfo = MemberInfo(
                    memberId = testMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                // 멤버가 해당 토큰을 가지고 있는 상황
                testMember.addDeviceToken(DeviceToken(token = testDeviceToken, isActive = true))

                every { loadMemberPort.loadById(testMemberId) } returns testMember
                every { saveMemberPort.update(any()) } returns testMember

                // When
                memberDeviceTokenUsecase.unregisterDeviceToken(testMemberInfo, testDeviceToken)

                // Then
                verify(exactly = 1) { loadMemberPort.loadById(testMemberId) }
                verify(exactly = 1) { saveMemberPort.update(testMember) }

                // DeviceToken의 equals는 token 기준으로 비교하므로 제거됨을 확인
                testMember.deviceTokens.none { it.token == testDeviceToken } shouldBe true
            }
        }

        When("존재하지 않는 멤버가 디바이스 토큰을 해제하려고 할 때") {
            Then("NotFoundException이 발생한다") {
                // Given
                val testMemberInfo = MemberInfo(
                    memberId = testMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                every { loadMemberPort.loadById(testMemberId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    memberDeviceTokenUsecase.unregisterDeviceToken(testMemberInfo, testDeviceToken)
                }

                exception.message shouldBe "존재하지 않는 멤버입니다."

                verify(exactly = 1) { loadMemberPort.loadById(testMemberId) }
                verify(exactly = 0) { saveMemberPort.update(any()) }
            }
        }

        When("멤버가 보유하지 않은 디바이스 토큰을 해제하려고 할 때") {
            Then("정상적으로 처리되고 변화가 없다") {
                // Given
                val testMember = Member(
                    id = ObjectId(testMemberId),
                    nickname = "testUser"
                )
                val testMemberInfo = MemberInfo(
                    memberId = testMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                every { loadMemberPort.loadById(testMemberId) } returns testMember
                every { saveMemberPort.update(any()) } returns testMember

                // When
                memberDeviceTokenUsecase.unregisterDeviceToken(testMemberInfo, testDeviceToken)

                // Then
                verify(exactly = 1) { loadMemberPort.loadById(testMemberId) }
                verify(exactly = 1) { saveMemberPort.update(testMember) }

                // 원래 토큰이 없었으므로 여전히 없어야 함
                testMember.deviceTokens.none { it.token == testDeviceToken } shouldBe true
            }
        }
    }
})
