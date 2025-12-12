package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.member.impl.DeleteMemberUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import org.bson.types.ObjectId

/**
 * MemberDeleteUsecaseImpl 클래스의 기능을 테스트한다.
 * 회원 삭제 기능의 모든 시나리오를 검증한다.
 */
class MemberDeleteUsecaseImplTest : BehaviorSpec({

    // 테스트 대상 클래스와 의존성 객체들
    val loadMemberPort = mockk<LoadMemberPort>()
    val saveMemberPort = mockk<SaveMemberPort>()
    val memberDeleteUsecase = DeleteMemberUseCaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort
    )

    beforeEach {
        // Mock 객체 초기화
        clearMocks(loadMemberPort, saveMemberPort)
    }

    Given("회원 삭제 요청이 들어왔을 때") {

        When("본인이 본인의 계정을 삭제하려고 하고, 회원이 존재하는 경우") {
            Then("회원 삭제가 성공적으로 수행되어야 한다") {
                // 테스트 데이터 생성
                val validMemberId = ObjectId.get().toHexString()
                val validMember = createMemberEntity(
                    id = ObjectId(validMemberId),
                    nickname = "testUser"
                )
                val validMemberInfo = MemberInfo(
                    memberId = validMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                // Mock 설정
                every { loadMemberPort.loadById(validMemberId) } returns validMember
                every { saveMemberPort.delete(validMember) } returns true

                // 실행
                memberDeleteUsecase.deleteMember(validMemberInfo, validMemberId)

                // 검증
                verify(exactly = 1) { loadMemberPort.loadById(validMemberId) }
                verify(exactly = 1) { saveMemberPort.delete(validMember) }
            }
        }

        When("본인이 본인의 계정을 삭제하려고 하지만, 회원이 존재하지 않는 경우") {
            Then("회원 조회 후 삭제 없이 정상 종료되어야 한다") {
                // 테스트 데이터 생성
                val validMemberId = ObjectId.get().toHexString()
                val validMemberInfo = MemberInfo(
                    memberId = validMemberId,
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                // Mock 설정
                every { loadMemberPort.loadById(validMemberId) } returns null

                // 실행
                memberDeleteUsecase.deleteMember(validMemberInfo, validMemberId)

                // 검증
                verify(exactly = 1) { loadMemberPort.loadById(validMemberId) }
                verify(exactly = 0) { saveMemberPort.delete(any<Member>()) }
            }
        }

        When("다른 사용자의 계정을 삭제하려고 하는 경우") {
            Then("ForbiddenException이 발생해야 한다") {
                // 테스트 데이터 생성
                val validMemberId = ObjectId.get().toHexString()
                val invalidMemberInfo = MemberInfo(
                    memberId = ObjectId.get().toHexString(),
                    nickname = "anotherUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                // 실행 및 검증
                val exception = shouldThrow<ForbiddenException> {
                    memberDeleteUsecase.deleteMember(invalidMemberInfo, validMemberId)
                }

                exception.message shouldBe "접근 권한이 없습니다."

                // 권한 검사에서 실패하므로 다른 메서드들은 호출되지 않아야 함
                verify(exactly = 0) { loadMemberPort.loadById(any<String>()) }
                verify(exactly = 0) { saveMemberPort.delete(any<Member>()) }
            }
        }

        When("로그인한 사용자의 ID가 빈 문자열이고 삭제 대상 ID가 유효한 경우") {
            Then("ForbiddenException이 발생해야 한다") {
                // 테스트 데이터 생성
                val validMemberId = ObjectId.get().toHexString()
                val emptyMemberIdInfo = MemberInfo(
                    memberId = "",
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                // 실행 및 검증
                val exception = shouldThrow<ForbiddenException> {
                    memberDeleteUsecase.deleteMember(emptyMemberIdInfo, validMemberId)
                }

                exception.message shouldBe "접근 권한이 없습니다."

                verify(exactly = 0) { loadMemberPort.loadById(any<String>()) }
                verify(exactly = 0) { saveMemberPort.delete(any<Member>()) }
            }
        }

        When("빈 문자열 회원 ID로 삭제를 시도하는 경우") {
            Then("ForbiddenException이 발생해야 한다") {
                // 테스트 데이터 생성
                val emptyMemberIdInfo = MemberInfo(
                    memberId = "",
                    nickname = "testUser",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                // 실행 및 검증
                val exception = shouldThrow<ForbiddenException> {
                    memberDeleteUsecase.deleteMember(emptyMemberIdInfo, "not-matched")
                }

                exception.message shouldBe "접근 권한이 없습니다."

                verify(exactly = 0) { loadMemberPort.loadById(any<String>()) }
                verify(exactly = 0) { saveMemberPort.delete(any<Member>()) }
            }
        }
    }
})
