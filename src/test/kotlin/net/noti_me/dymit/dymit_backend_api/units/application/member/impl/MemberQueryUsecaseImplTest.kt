package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.member.impl.MemberQueryUsecaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort

class MemberQueryUsecaseImplTest : BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val memberQueryUsecase = MemberQueryUsecaseImpl(loadMemberPort)

    private val member = Member(
        id = "test-id",
        nickname = "test-nickname",
        oidcIdentities = mutableSetOf(OidcIdentity(
            provider = "GOOGLE",
            subject = "test-subject",
            email = "test@gmail.com"
        ))
    )

    private val memberInfo = MemberInfo(
        memberId = "test-id",
        nickname = "test-nickname",
        roles = listOf(MemberRole.ROLE_MEMBER)
    )

    init {

        beforeEach {

        }

        afterEach { clearAllMocks() }

        given("존재하는 회원 ID가 주어진다.") {
            val memberId = "test-id"

            `when`("현재 로그인 된 멤버의 ID와 조회 멤버 ID가 일치하면") {
                every { loadMemberPort.loadById(memberId) } returns member

                then("멤버 정보를 반환한다") {
                    val result = memberQueryUsecase.getMemberById(memberInfo, memberId)
                }
            }

            `when`("현재 로그인 된 멤버의 ID와 조회 멤버 ID가 일치하지 않으면") {
                val otherMemberInfo = MemberInfo(
                    memberId = "other-id",
                    nickname = "other-nickname",
                    roles = listOf(MemberRole.ROLE_MEMBER)
                )

                every { loadMemberPort.loadById(memberId) } returns member

                then("ForbiddenException을 발생시킨다") {
                    shouldThrow<ForbiddenException> {
                        memberQueryUsecase.getMemberById(otherMemberInfo, memberId)
                    }
                }
            }
        }

        given("존재하지 않는 회원 ID가 주어진다.") {
            val memberId = "non-existent-id"

            `when`("회원 정보를 조회하면") {
                every { loadMemberPort.loadById(memberId) } returns null

                then("NotFoundException을 발생시킨다") {
                    shouldThrowExactly<net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException> {
                        memberQueryUsecase.getMemberById(memberInfo, memberId)
                    }
                }
            }
        }
    }
}