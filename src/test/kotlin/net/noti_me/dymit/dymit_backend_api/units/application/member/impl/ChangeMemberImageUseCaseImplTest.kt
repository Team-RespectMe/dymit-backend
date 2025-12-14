package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UploadProfileImageUseCase
import net.noti_me.dymit.dymit_backend_api.application.member.dto.UpdateMemberProfileImageCommand
import net.noti_me.dymit.dymit_backend_api.application.member.impl.ChangeMemberImageUseCaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberPresetImage
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.supports.createMemberInfo
import org.bson.types.ObjectId

internal class ChangeMemberImageUseCaseImplTest(): BehaviorSpec() {

    private val loadMemberPort = mockk<LoadMemberPort>()

    private val saveMemberPort = mockk<SaveMemberPort>()

    private val uploadProfileImageUseCase = mockk<UploadProfileImageUseCase>()

    private val usecase = ChangeMemberImageUseCaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort,
        uploadProfileImageUseCase = uploadProfileImageUseCase
    )

    private val memberId = ObjectId.get()

    private val otherId = ObjectId.get()

    private var member: Member = createMemberEntity(id = memberId)

    private var other: Member = createMemberEntity(id = otherId)

    private var memberInfo = createMemberInfo(member)

    private var otherInfo = createMemberInfo(other)

    init {

        beforeEach {
            member = createMemberEntity(id = memberId)
            other = createMemberEntity(id = otherId)
            memberInfo = createMemberInfo(member)
            otherInfo = createMemberInfo(other)
        }

        afterEach {
            clearAllMocks()
        }

        Given("프로필 이미지 변경 요청이 주어진다.") {
            val command = UpdateMemberProfileImageCommand(
                memberId = member.identifier,
                type = ProfileImageType.PRESET,
                preset = MemberPresetImage.PROJECT
            )

            When("로그인한 멤버와 요청한 멤버가 다를 경우") {
                Then("ForbiddenException 예외가 발생한다.") {
                    shouldThrow<ForbiddenException> {
                        usecase.changeProfileImage(otherInfo, command)
                    }
                }
            }

            When("존재하지 않는 멤버라면") {
                every { loadMemberPort.loadById(any<String>()) } returns null
                Then("NotFoundException 예외가 발생한다.") {
                    shouldThrow<NotFoundException> {
                        usecase.changeProfileImage(memberInfo, command)
                    }
                }
            }

            When("프로필 이미지 타입이 EXTERNAL 인데 이미지 파일이 주어지지 않는다면") {
                every { loadMemberPort.loadById(any<String>()) } returns member
                val otherCommand = UpdateMemberProfileImageCommand(
                    memberId = member.identifier,
                    type = ProfileImageType.EXTERNAL,
                    preset = null
                )

                Then("BadRequestException 예외가 발생한다.") {
                    shouldThrow <BadRequestException> {
                        usecase.changeProfileImage(memberInfo, otherCommand)
                    }
                }
            }

            When("프로필 이미지 타입이 PRESET 인데 프리셋 이름이 주어지지 않는다면") {
                every { loadMemberPort.loadById(any<String>()) } returns member
                val otherCommand = UpdateMemberProfileImageCommand(
                    memberId = member.identifier,
                    type = ProfileImageType.PRESET,
                    preset = null
                )
                Then("BadRequestException 예외가 발생한다.") {
                    shouldThrow <BadRequestException> {
                        usecase.changeProfileImage(memberInfo, otherCommand)
                    }
                }
            }

            When("모두 정상인 경우") {
                Then("멤버의 프로필 이미지가 변경된다.") {
                    every { loadMemberPort.loadById(any<String>()) } returns member
                    every { saveMemberPort.update(any<Member>()) } returns member
                    usecase.changeProfileImage(memberInfo, command)
                    member.profileImage.type shouldBe command.type
                    member.profileImage.thumbnail shouldBe MemberPresetImage.PROJECT.thumbnail
                    member.profileImage.original shouldBe MemberPresetImage.PROJECT.original
                }
            }
        }
    }
}