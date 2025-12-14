package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.member.impl.QueryMemberUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId

internal class QueryMemberUseCaseImplTest()
: BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val queryMemberUseCase = QueryMemberUseCaseImpl(loadMemberPort)

    private val memberId = ObjectId.get()

    private val member = createMemberEntity(id=memberId)

    private val memberInfo = createMemberInfo(member)


    init {
        afterEach {
            clearAllMocks()
        }

        Given("검색 할 회원 식별자가 주어진다.") {
            When("회원이 존재하지 않으면") {
                every { loadMemberPort.loadById( memberId.toHexString() ) } returns null

                Then("NotFoundException이 발생한다." ) {
                    shouldThrow<NotFoundException> {
                        queryMemberUseCase.getMemberById(loginMember = memberInfo, memberId = memberId.toHexString())
                    }
                }
            }

            When("다른 사람을 조회하면") {
                every { loadMemberPort.loadById( any<String>() ) } returns member

                Then("ForbiddenException이 발생한다." ) {
                    shouldThrow<ForbiddenException> {
                        queryMemberUseCase.getMemberById(
                            loginMember = memberInfo,
                            memberId = ObjectId.get().toHexString()
                        )
                    }
                }
            }

            When("자기 자신을 조회하면") {
                every { loadMemberPort.loadById( memberId.toHexString() ) } returns member

                Then("정상적으로 조회된다." ) {
                    val result = queryMemberUseCase.getMemberById(
                        loginMember = memberInfo,
                        memberId = memberId.toHexString()
                    )

                    result.id shouldBe memberId.toHexString()
                }
            }
        }
    }
}