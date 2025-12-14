package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.member.impl.DeleteMemberUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId

internal class DeleteMemberUseCaseImplTest(): BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val loadGroupPort = mockk<LoadStudyGroupPort>()

    private val deleteMemberUseCase = DeleteMemberUseCaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort,
        loadGroupPort = loadGroupPort
    )

    private val memberId = ObjectId.get()

    private val otherId = ObjectId.get()

    private var member = createMemberEntity(memberId)

    private var other = createMemberEntity(otherId)

    private var memberInfo = createMemberInfo(member)

    private var otherInfo = createMemberInfo(other)

    init {

        beforeContainer {
            member = createMemberEntity(memberId)
            other = createMemberEntity(otherId)
            memberInfo = createMemberInfo(member)
            otherInfo = createMemberInfo(other)

            every { loadMemberPort.loadById(memberId.toString()) } returns member
            every { loadMemberPort.loadById(otherId.toString()) } returns other
            every { loadGroupPort.loadByOwnerId(memberId.toString() ) } returns emptyList()
            every { saveMemberPort.update(member) } returns member
        }

        Given("삭제할 회원의 ID가 주어진다.") {
            val targetId = memberId
            When("요청 회원과 다르면") {
                Then("예외가 발생한다.") {
                    shouldThrow<ForbiddenException>{
                        deleteMemberUseCase.deleteMember(
                            loginMember = otherInfo,
                            memberId = targetId.toString()
                        )
                    }
                }
            }

            When("회원이 존재하지 않으면") {
                every { loadMemberPort.loadById(targetId.toString()) } returns null
                Then("종료된다.") {
                    deleteMemberUseCase.deleteMember(
                        loginMember = memberInfo,
                        memberId = targetId.toString()
                    )
                }
            }

            When("회원이 스터디 그룹을 소유하고 있으면") {
                every { loadGroupPort.loadByOwnerId(memberId.toString() ) } returns listOf(
                    mockk()
                )
                Then("예외가 발생한다.") {
                    shouldThrow<ForbiddenException>{
                        deleteMemberUseCase.deleteMember(
                            loginMember = memberInfo,
                            memberId = targetId.toString()
                        )
                    }
                }
            }

            When("정상적인 요청이면") {
                Then("회원이 탈퇴 처리된다.") {
                    deleteMemberUseCase.deleteMember(
                        loginMember = memberInfo,
                        memberId = targetId.toString()
                    )
                }
            }
        }
    }
}