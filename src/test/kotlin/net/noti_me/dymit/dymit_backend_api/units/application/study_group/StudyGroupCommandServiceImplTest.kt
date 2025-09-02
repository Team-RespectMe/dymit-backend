package net.noti_me.dymit.dymit_backend_api.units.application.study_group

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.board.BoardService
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupCommandServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupImageUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupJoinCommand
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher

/**
 * StudyGroupCommandServiceImpl 테스트 클래스
 * 스터디 그룹 명령 서비스의 모든 공용 메서드와 분기를 테스트합니다.
 */
class StudyGroupCommandServiceImplTest : BehaviorSpec({

    // Mock 객체들
    val loadMemberPort = mockk<LoadMemberPort>()
    val loadStudyGroupPort = mockk<LoadStudyGroupPort>()
    val saveStudyGroupPort = mockk<SaveStudyGroupPort>()
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()
    val boardService = mockk<BoardService>()
    val applicationEventPublisher = mockk<ApplicationEventPublisher>()

    // 테스트 대상 서비스
    val studyGroupCommandService = StudyGroupCommandServiceImpl(
        loadMemberPort = loadMemberPort,
        loadStudyGroupPort = loadStudyGroupPort,
        saveStudyGroupPort = saveStudyGroupPort,
        studyGroupMemberRepository = studyGroupMemberRepository,
        boardService = boardService,
        applicationEventPublisher = applicationEventPublisher
    )

    // 공통 테스트 데이터
    var memberInfo = MemberInfo("", "", emptyList())
    var member = Member()
    var studyGroup = StudyGroup()
    var studyGroupMember = StudyGroupMember(
        profileImage = MemberProfileImageVo(type = "preset", url = "0")
    )
    val memberId = ObjectId.get()
    val groupId = ObjectId.get()
    val ownerId = ObjectId.get()

    beforeEach {
        memberInfo = MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )

        member = Member(
            id = memberId,
            nickname = "testUser",
            profileImage = MemberProfileImageVo(type = "preset", url = "0")
        )

        studyGroup = StudyGroup(
            id = groupId,
            ownerId = ownerId,
            name = "Test Study Group",
            description = "Test Description"
        )

        studyGroupMember = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "testUser",
            profileImage = MemberProfileImageVo(type = "preset", url = "0"),
            role = GroupMemberRole.MEMBER
        )
    }

    afterEach {
        clearAllMocks()
    }

    /**
     * 스터디 그룹 생성 커맨드 객체를 생성하는 헬퍼 함수
     */
    fun createStudyGroupCreateCommand(
        name: String = "Test Group",
        description: String = "Test Description"
    ) = StudyGroupCreateCommand(name = name, description = description)

    /**
     * 스터디 그룹 가입 커맨드 객체를 생성하는 헬퍼 함수
     */
    fun createStudyGroupJoinCommand(
        inviteCode: String = "TESTCODE",
        groupId: String = "testGroupId"
    ) = StudyGroupJoinCommand(inviteCode = inviteCode, groupId = groupId)

    /**
     * 스터디 그룹 이미지 업데이트 커맨드 객체를 생성하는 헬퍼 함수
     */
    fun createStudyGroupImageUpdateCommand(
        groupId: String = "testGroupId",
        type: String = "preset",
        value: Int? = 1
    ) = StudyGroupImageUpdateCommand(groupId = groupId, type = type, value = value, file = null)

    /**
     * 테스트용 BoardDto Mock 객체를 생성하는 헬퍼 함수
     */
    fun createBoardDto() = BoardDto(
        id = "boardId",
        groupId = groupId.toHexString(),
        name = "공지 사항",
        createdAt = java.time.LocalDateTime.now(),
        permissions = mutableListOf()
    )

    given("사용자가 스터디 그룹을 생성할 때") {

        `when`("유효한 정보로 스터디 그룹을 생성하면") {
            val command = createStudyGroupCreateCommand()
            val savedStudyGroup = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = command.name,
                description = command.description
            )
            val savedOwner = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = member.nickname,
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.OWNER
            )

            every { loadMemberPort.loadById(memberInfo.memberId) } returns member
            every { loadStudyGroupPort.existsByInviteCode(any()) } returns false
            every { saveStudyGroupPort.persist(any()) } returns savedStudyGroup
            every { studyGroupMemberRepository.persist(any()) } returns savedOwner
            every { applicationEventPublisher.publishEvent(any()) } returns Unit
            every { boardService.createBoard(any(), any(), any()) } returns createBoardDto()

            val result = studyGroupCommandService.createStudyGroup(memberInfo, command)

            then("스터디 그룹이 성공적으로 생성된다") {
                result shouldNotBe null
                result.name shouldBe command.name
                result.description shouldBe command.description
            }
        }

        `when`("존재하지 않는 멤버가 스터디 그룹을 생성하려고 하면") {
            val command = createStudyGroupCreateCommand()

            every { loadMemberPort.loadById(memberInfo.memberId) } returns null

            then("멤버를 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupCommandService.createStudyGroup(memberInfo, command)
                }
                exception.message shouldBe "존재하지 않는 멤버입니다."
            }
        }

        `when`("초대 코드가 중복되어 재생성이 필요한 경우") {
            val command = createStudyGroupCreateCommand()
            val savedStudyGroup = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = command.name,
                description = command.description
            )
            val savedOwner = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = member.nickname,
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.OWNER
            )

            every { loadMemberPort.loadById(memberInfo.memberId) } returns member
            every { loadStudyGroupPort.existsByInviteCode(any()) } returnsMany listOf(true, false)
            every { saveStudyGroupPort.persist(any()) } returns savedStudyGroup
            every { studyGroupMemberRepository.persist(any()) } returns savedOwner
            every { applicationEventPublisher.publishEvent(any()) } returns Unit
            every { boardService.createBoard(any(), any(), any()) } returns createBoardDto()

            val result = studyGroupCommandService.createStudyGroup(memberInfo, command)

            then("중복되지 않는 초대 코드로 스터디 그룹이 생성된다") {
                result shouldNotBe null
                result.name shouldBe command.name
                result.description shouldBe command.description
            }
        }
    }

    given("사용자가 스터디 그룹에 가입할 때") {

        `when`("유효한 초대 코드로 가입하면") {
            val command = createStudyGroupJoinCommand()
            val joinedMember = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = member.nickname,
                profileImage = member.profileImage,
                role = GroupMemberRole.MEMBER
            )

            every { loadStudyGroupPort.loadByInviteCode(command.inviteCode) } returns studyGroup
            every { loadMemberPort.loadById(memberInfo.memberId) } returns member
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(studyGroup.id, member.id) } returns null
            every { studyGroupMemberRepository.persist(any()) } returns joinedMember

            val result = studyGroupCommandService.joinStudyGroup(memberInfo, command)

            then("스터디 그룹에 성공적으로 가입된다") {
                result shouldNotBe null
                result.groupId shouldBe groupId.toHexString()
                result.nickname shouldBe member.nickname
                result.role shouldBe GroupMemberRole.MEMBER
            }
        }

        `when`("존재하지 않는 초대 코드로 가입하려고 하면") {
            val command = createStudyGroupJoinCommand(inviteCode = "INVALID")

            every { loadStudyGroupPort.loadByInviteCode(command.inviteCode) } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupCommandService.joinStudyGroup(memberInfo, command)
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("존재하지 않는 멤버가 가입하려고 하면") {
            val command = createStudyGroupJoinCommand()

            every { loadStudyGroupPort.loadByInviteCode(command.inviteCode) } returns studyGroup
            every { loadMemberPort.loadById(memberInfo.memberId) } returns null

            then("멤버를 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupCommandService.joinStudyGroup(memberInfo, command)
                }
                exception.message shouldBe "존재하지 않는 멤버입니다."
            }
        }

        `when`("이미 가입된 스터디 그룹에 다시 가입하려고 하면") {
            val command = createStudyGroupJoinCommand()

            every { loadStudyGroupPort.loadByInviteCode(command.inviteCode) } returns studyGroup
            every { loadMemberPort.loadById(memberInfo.memberId) } returns member
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(studyGroup.id, member.id) } returns studyGroupMember

            then("이미 가입되어 있다는 예외가 발생한다") {
                val exception = shouldThrow<ConflictException> {
                    studyGroupCommandService.joinStudyGroup(memberInfo, command)
                }
                exception.message shouldBe "이미 해당 스터디 그룹에 가입되어 있습니다."
            }
        }
    }

    given("사용자가 스터디 그룹 프로필 이미지를 업데이트할 때") {

        `when`("유효한 preset 이미지로 업데이트하면") {
            val command = createStudyGroupImageUpdateCommand(
                groupId = groupId.toHexString(),
                value = 3
            )
            val ownerStudyGroup = StudyGroup(
                id = groupId,
                ownerId = memberId, // memberInfo.memberId와 일치하도록 수정
                name = "Test Group",
                description = "Test Description"
            )
            val updatedGroup = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = "Test Group",
                description = "Test Description",
                profileImage = GroupProfileImageVo(type = "preset", url = "3")
            )

            every { loadStudyGroupPort.loadByGroupId(command.groupId) } returns ownerStudyGroup
            every { saveStudyGroupPort.update(any()) } returns updatedGroup

            val result = studyGroupCommandService.updateStudyGroupProfileImage(memberInfo, command)

            then("프로필 이미지가 성공적으로 업데이트된다") {
                result shouldNotBe null
                result.profileImage.type shouldBe "preset"
                result.profileImage.url shouldBe "3"
            }
        }

        `when`("존재하지 않는 스터디 그룹의 이미지를 업데이트하려고 하면") {
            val command = createStudyGroupImageUpdateCommand()

            every { loadStudyGroupPort.loadByGroupId(command.groupId) } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupCommandService.updateStudyGroupProfileImage(memberInfo, command)
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("소유자가 아닌 사용자가 이미지를 업데이트하려고 하면") {
            val command = createStudyGroupImageUpdateCommand()
            val nonOwnerStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId, // 다른 사용자가 ���유자인 경우
                name = "Test Group",
                description = "Test Description"
            )

            every { loadStudyGroupPort.loadByGroupId(command.groupId) } returns nonOwnerStudyGroup

            then("소유자가 아니라는 예외가 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyGroupCommandService.updateStudyGroupProfileImage(memberInfo, command)
                }
                exception.message shouldBe "그룹 소유자가 아닙니다."
            }
        }

        `when`("잘못된 preset 타입으로 업데이트하려고 하면") {
            val command = createStudyGroupImageUpdateCommand(type = "invalid")
            val ownerStudyGroup = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = "Test Group",
                description = "Test Description"
            )

            every { loadStudyGroupPort.loadByGroupId(command.groupId) } returns ownerStudyGroup

            then("잘못된 이미지 타입이라는 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    studyGroupCommandService.updateStudyGroupProfileImage(memberInfo, command)
                }
            }
        }

        `when`("preset value가 null인 경우") {
            val command = createStudyGroupImageUpdateCommand(value = null)
            val ownerStudyGroup = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = "Test Group",
                description = "Test Description"
            )

            every { loadStudyGroupPort.loadByGroupId(command.groupId) } returns ownerStudyGroup

            then("잘못된 Preset Value라는 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    studyGroupCommandService.updateStudyGroupProfileImage(memberInfo, command)
                }
                exception.message shouldBe "잘못된 Preset Value입니다."
            }
        }

        `when`("preset value가 범위를 벗어난 경우") {
            val command = createStudyGroupImageUpdateCommand(value = 10)
            val ownerStudyGroup = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = "Test Group",
                description = "Test Description"
            )

            every { loadStudyGroupPort.loadByGroupId(command.groupId) } returns ownerStudyGroup

            then("존재하지 않는 Preset Value라는 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    studyGroupCommandService.updateStudyGroupProfileImage(memberInfo, command)
                }
                exception.message shouldBe "존재하지 않는 Preset Value입니다."
            }
        }

        `when`("preset value가 음수인 경우") {
            val command = createStudyGroupImageUpdateCommand(value = -1)
            val ownerStudyGroup = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = "Test Group",
                description = "Test Description"
            )

            every { loadStudyGroupPort.loadByGroupId(command.groupId) } returns ownerStudyGroup

            then("존재하지 않는 Preset Value라는 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    studyGroupCommandService.updateStudyGroupProfileImage(memberInfo, command)
                }
                exception.message shouldBe "존재하지 않는 Preset Value입니다."
            }
        }
    }

    given("사용자가 스터디 그룹을 삭제할 때") {

        `when`("소유자가 혼자 있는 스터디 그룹을 삭제하면") {
            val ownerMemberInfo = MemberInfo(
                memberId = ownerId.toHexString(),
                nickname = "owner",
                roles = listOf(MemberRole.ROLE_MEMBER)
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup
            every { studyGroupMemberRepository.countByGroupId(studyGroup.id) } returns 1L
            every { saveStudyGroupPort.delete(studyGroup) } returns true

            val result = studyGroupCommandService.deleteStudyGroup(ownerMemberInfo, groupId.toHexString())

            then("스터디 그룹이 성공적으로 삭제된다") {
                result shouldBe true
            }
        }

        `when`("존재하지 않는 스터디 그룹을 삭제하려고 하면") {
            every { loadStudyGroupPort.loadByGroupId("invalid") } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupCommandService.deleteStudyGroup(memberInfo, "invalid")
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("소유자가 아닌 사용자가 스터디 그룹을 삭제하려고 하면") {
            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup

            then("삭제 권한이 없다는 예외가 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyGroupCommandService.deleteStudyGroup(memberInfo, groupId.toHexString())
                }
                exception.message shouldBe "스터디 그룹 삭제 권한이 없습니다."
            }
        }

        `when`("다른 멤버가 있는 스터디 그룹을 삭제하려고 하면") {
            val ownerMemberInfo = MemberInfo(
                memberId = ownerId.toHexString(),
                nickname = "owner",
                roles = listOf(MemberRole.ROLE_MEMBER)
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup
            every { studyGroupMemberRepository.countByGroupId(studyGroup.id) } returns 3L

            then("다른 멤버가 있어 삭제할 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    studyGroupCommandService.deleteStudyGroup(ownerMemberInfo, groupId.toHexString())
                }
                exception.message shouldBe "스터디 그룹에 다른 멤버가 있어 삭제할 수 없습니다."
            }
        }
    }

    given("사용자가 스터디 그룹을 탈퇴할 때") {

        `when`("일반 멤버가 스터디 그룹을 탈퇴하면") {
            val membershipToDelete = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = member.nickname,
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.MEMBER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, ObjectId(memberInfo.memberId)) } returns membershipToDelete
            every { studyGroupMemberRepository.delete(membershipToDelete) } returns true

            studyGroupCommandService.leaveStudyGroup(memberInfo, groupId.toHexString())

            then("스터디 그룹에서 성공적으로 탈퇴한다") {
                // 예외가 발생하지 않으면 성공
            }
        }

        `when`("존재하지 않는 스터디 그룹에서 탈퇴하려고 하면") {
            every { loadStudyGroupPort.loadByGroupId("invalid") } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupCommandService.leaveStudyGroup(memberInfo, "invalid")
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("해당 스터디 그룹의 멤버가 아닌 사용자가 탈퇴하려고 하면") {
            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, ObjectId(memberInfo.memberId)) } returns null

            then("해당 스터디 그룹의 멤버가 아니라는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupCommandService.leaveStudyGroup(memberInfo, groupId.toHexString())
                }
                exception.message shouldBe "해당 스터디 그룹의 멤버가 아닙니다."
            }
        }

        `when`("다른 멤버가 있는 스터디 그룹에서 그룹장이 탈퇴하려고 하면") {
            val studyGroupWithMultipleMembers = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = "Test Group",
                description = "Test Description",
                memberCount = 3
            )
            val ownerMembership = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = member.nickname,
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.OWNER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroupWithMultipleMembers
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, ObjectId(memberInfo.memberId)) } returns ownerMembership

            then("스터디 그룹장은 탈퇴할 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<ConflictException> {
                    studyGroupCommandService.leaveStudyGroup(memberInfo, groupId.toHexString())
                }
                exception.message shouldBe "스터디 그룹장은 스터디 그룹을 탈퇴할 수 없습니다. 다른 인원을 모두 탈퇴시킨 뒤 다시 시도하세요."
            }
        }

        `when`("혼자 있는 스터디 그룹에서 그룹장이 탈퇴하면") {
            val studyGroupWithSingleMember = StudyGroup(
                id = groupId,
                ownerId = memberId,
                name = "Test Group",
                description = "Test Description",
                memberCount = 1
            )
            val ownerMembership = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = member.nickname,
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.OWNER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroupWithSingleMember
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, ObjectId(memberInfo.memberId)) } returns ownerMembership
            every { studyGroupMemberRepository.delete(ownerMembership) } returns true


            then("스터디 그룹에서 성공적으로 탈퇴한다") {
                // 예외가 발생하지 않으면 성공
                shouldNotThrowAny {
                    studyGroupCommandService.leaveStudyGroup(memberInfo, groupId.toHexString())
                }
            }
        }
    }
})
