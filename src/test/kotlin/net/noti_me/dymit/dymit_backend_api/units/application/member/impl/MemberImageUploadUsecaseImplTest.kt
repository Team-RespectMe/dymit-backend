package net.noti_me.dymit.dymit_backend_api.units.application.member.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.member.impl.MemberImageUploadUsecaseImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotImplementedException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import org.bson.types.ObjectId
import org.springframework.web.multipart.MultipartFile

/**
 * MemberImageUploadUsecaseImpl 클래스에 대한 테스트
 * 멤버 이미지 업로드 유스케이스의 모든 기능과 예외 상황을 테스트한다
 */
internal class MemberImageUploadUsecaseImplTest : BehaviorSpec({

    val loadMemberPort = mockk<LoadMemberPort>()
    val saveMemberPort = mockk<SaveMemberPort>()
    val multipartFile = mockk<MultipartFile>()

    val memberImageUploadUsecase = MemberImageUploadUsecaseImpl(
        loadMemberPort = loadMemberPort,
        saveMemberPort = saveMemberPort
    )

    beforeEach {
    }

    afterEach {
        clearAllMocks()
    }

    Given("정상적인 멤버 정보와 프리셋 번호가 주어졌을 때") {
        val memberId = "507f1f77bcf86cd799439011"
        val loginMember = MemberInfo(
            memberId = memberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val type = "profile"
        val presetNo = 3

        val existingMember = Member(
            id = ObjectId(memberId),
            nickname = "testUser",
            profileImage = MemberProfileImageVo(
                filePath = "",
                fileSize = 0L,
                url = "0",
                type = "preset",
                width = 0,
                height = 0
            )
        )

        val updatedMember = Member(
            id = ObjectId(memberId),
            nickname = "testUser",
            profileImage = MemberProfileImageVo(
                filePath = "",
                fileSize = 0L,
                url = "3",
                type = "presets",
                width = 0,
                height = 0
            )
        )

        When("프리셋 이미지로 업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns existingMember
            every { saveMemberPort.update(any()) } returns updatedMember

            val result = memberImageUploadUsecase.uploadImage(
                loginMember = loginMember,
                memberId = memberId,
                type = type,
                presetNo = presetNo,
                imageFile = null
            )

            Then("멤버의 프로필 이미지가 프리셋으로 업데이트되어야 한다") {
                result shouldNotBe null
                result.id shouldBe memberId
                result.profileImage?.url shouldBe "3"
                result.profileImage?.type shouldBe "presets"

                verify(exactly = 1) { loadMemberPort.loadById(memberId) }
                verify(exactly = 1) { saveMemberPort.update(any()) }
            }
        }
    }

    Given("다른 멤버의 이미지 업로드를 시도할 때") {
        val loginMemberId = "507f1f77bcf86cd799439011"
        val targetMemberId = "507f1f77bcf86cd799439012"
        val loginMember = MemberInfo(
            memberId = loginMemberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val type = "profile"
        val presetNo = 3

        When("다른 멤버의 ID로 업로드를 요청하면") {
            val exception = shouldThrow<ForbiddenException> {
                memberImageUploadUsecase.uploadImage(
                    loginMember = loginMember,
                    memberId = targetMemberId,
                    type = type,
                    presetNo = presetNo,
                    imageFile = null
                )
            }

            Then("ForbiddenException이 발생해야 한다") {
                exception.message shouldBe "허용되지 않는 리소스 접근입니다."

                verify(exactly = 0) { loadMemberPort.loadById(any<String>()) }
                verify(exactly = 0) { saveMemberPort.update(any()) }
            }
        }
    }

    Given("존재하지 않는 멤버 ID가 주어졌을 때") {
        val memberId = "507f1f77bcf86cd799439011"
        val loginMember = MemberInfo(
            memberId = memberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val type = "profile"
        val presetNo = 3

        When("업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns null

            val exception = shouldThrow<NotFoundException> {
                memberImageUploadUsecase.uploadImage(
                    loginMember = loginMember,
                    memberId = memberId,
                    type = type,
                    presetNo = presetNo,
                    imageFile = null
                )
            }

            Then("NotFoundException이 발생해야 한다") {
                exception.message shouldBe "존재하지 않는 멤버입니다."

                verify(exactly = 1) { loadMemberPort.loadById(memberId) }
                verify(exactly = 0) { saveMemberPort.update(any()) }
            }
        }
    }

    Given("이미지 파일이 주어졌을 때") {
        val memberId = "507f1f77bcf86cd799439011"
        val loginMember = MemberInfo(
            memberId = memberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val type = "profile"

        val existingMember = Member(
            id = ObjectId(memberId),
            nickname = "testUser"
        )

        When("이미지 파일로 업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns existingMember
            every { multipartFile.originalFilename } returns "test.jpg"
            every { multipartFile.size } returns 1024L

            val exception = shouldThrow<NotImplementedException> {
                memberImageUploadUsecase.uploadImage(
                    loginMember = loginMember,
                    memberId = memberId,
                    type = type,
                    presetNo = null,
                    imageFile = multipartFile
                )
            }

            Then("NotImplementedException이 발생해야 한다") {
                exception.message shouldBe "이미지 업로드 기능은 아직 구현되지 않았습니다."

                verify(exactly = 1) { loadMemberPort.loadById(memberId) }
                verify(exactly = 0) { saveMemberPort.update(any()) }
            }
        }
    }

    Given("이미지 파일과 프리셋 번호가 모두 null일 때") {
        val memberId = "507f1f77bcf86cd799439011"
        val loginMember = MemberInfo(
            memberId = memberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val type = "profile"

        val existingMember = Member(
            id = ObjectId(memberId),
            nickname = "testUser"
        )

        When("업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns existingMember

            val exception = shouldThrow<BadRequestException> {
                memberImageUploadUsecase.uploadImage(
                    loginMember = loginMember,
                    memberId = memberId,
                    type = type,
                    presetNo = null,
                    imageFile = null
                )
            }

            Then("BadRequestException이 발생해야 한다") {
                exception.message shouldBe "이미지 업로드를 위한 이미지 파일 또는 프리셋 번호가 필요합니다."

                verify(exactly = 1) { loadMemberPort.loadById(memberId) }
                verify(exactly = 0) { saveMemberPort.update(any()) }
            }
        }
    }

    Given("여러 다른 프리셋 번호들이 주어졌을 때") {
        val memberId = "507f1f77bcf86cd799439011"
        val loginMember = MemberInfo(
            memberId = memberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val type = "profile"

        val existingMember = Member(
            id = ObjectId(memberId),
            nickname = "testUser"
        )

        When("프리셋 번호 0으로 업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns existingMember
            every { saveMemberPort.update(any()) } answers { firstArg() }

            val result = memberImageUploadUsecase.uploadImage(
                loginMember = loginMember,
                memberId = memberId,
                type = type,
                presetNo = 0,
                imageFile = null
            )

            Then("프리셋 이미지 URL이 0으로 설정되어야 한다") {
                result.profileImage?.url shouldBe "0"
                result.profileImage?.type shouldBe "presets"
            }
        }

        When("프리셋 번호 5로 업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns existingMember
            every { saveMemberPort.update(any()) } answers { firstArg() }

            val result = memberImageUploadUsecase.uploadImage(
                loginMember = loginMember,
                memberId = memberId,
                type = type,
                presetNo = 5,
                imageFile = null
            )

            Then("프리셋 이미지 URL이 5로 설정되어야 한다") {
                result.profileImage?.url shouldBe "5"
                result.profileImage?.type shouldBe "presets"
            }
        }
    }

    Given("다양한 type 값들이 주어졌을 때") {
        val memberId = "507f1f77bcf86cd799439011"
        val loginMember = MemberInfo(
            memberId = memberId,
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
        val presetNo = 1

        val existingMember = Member(
            id = ObjectId(memberId),
            nickname = "testUser"
        )

        When("type이 'avatar'로 업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns existingMember
            every { saveMemberPort.update(any()) } answers { firstArg() }

            val result = memberImageUploadUsecase.uploadImage(
                loginMember = loginMember,
                memberId = memberId,
                type = "avatar",
                presetNo = presetNo,
                imageFile = null
            )

            Then("성공적으로 업로드되어야 한다") {
                result shouldNotBe null
                result.profileImage?.url shouldBe "1"
                result.profileImage?.type shouldBe "presets"
            }
        }

        When("type이 'thumbnail'로 업로드를 요청하면") {
            every { loadMemberPort.loadById(memberId) } returns existingMember
            every { saveMemberPort.update(any()) } answers { firstArg() }

            val result = memberImageUploadUsecase.uploadImage(
                loginMember = loginMember,
                memberId = memberId,
                type = "thumbnail",
                presetNo = presetNo,
                imageFile = null
            )

            Then("성공적으로 업로드되어야 한다") {
                result shouldNotBe null
                result.profileImage?.url shouldBe "1"
                result.profileImage?.type shouldBe "presets"
            }
        }
    }
})
