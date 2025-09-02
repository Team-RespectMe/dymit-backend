package net.noti_me.dymit.dymit_backend_api.units.application.study_group

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.study_group.StudyGroupQueryServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
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
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * StudyGroupQueryServiceImpl 테스트 클래스
 * 스터디 그룹 조회 서비스의 모든 공용 메서드와 분기를 테스트합니다.
 */
class StudyGroupQueryServiceImplTest : BehaviorSpec({

    // Mock 객체들
    val loadStudyGroupPort = mockk<LoadStudyGroupPort>()
    val loadMemberPort = mockk<LoadMemberPort>()
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()
    val saveStudyGroupPort = mockk<SaveStudyGroupPort>()
    val scheduleRepository = mockk<StudyScheduleRepository>()

    // 테스트 대상 서비스
    val studyGroupQueryService = StudyGroupQueryServiceImpl(
        loadStudyGroupPort = loadStudyGroupPort,
        loadMemberPort = loadMemberPort,
        studyGroupMemberRepository = studyGroupMemberRepository,
        saveStudyGroupPort = saveStudyGroupPort,
        scheduleRepository = scheduleRepository
    )

    // 테스트용 데이터
    var memberInfo = MemberInfo("", "", emptyList())
    var studyGroup = StudyGroup()
    var owner = Member()
    var studyGroupMember = StudyGroupMember(
        profileImage = MemberProfileImageVo(type = "preset", url = "0")
    )
    var inviteCode = InviteCodeVo()
    val groupId = ObjectId.get()
    val memberId = ObjectId.get()
    val ownerId = ObjectId.get()

    beforeEach {
        // 테스트용 객체 초기화
        memberInfo = MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "testNickname",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )

        inviteCode = InviteCodeVo(
            code = "TESTCODE",
            createdAt = LocalDateTime.now().minusDays(1),
            expireAt = LocalDateTime.now().plusDays(7)
        )

        studyGroup = StudyGroup(
            id = groupId,
            ownerId = ownerId,
            name = "Test Study Group",
            description = "Test Description",
            memberCount = 5,
            profileImage = GroupProfileImageVo(type = "preset", url = "0"),
            inviteCode = inviteCode
        )

        owner = Member(
            id = ownerId,
            nickname = "Owner Nickname",
            profileImage = MemberProfileImageVo(type = "preset", url = "0")
        )

        studyGroupMember = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "testNickname",
            profileImage = MemberProfileImageVo(type = "preset", url = "0"),
            role = GroupMemberRole.MEMBER
        )
    }

    afterEach {
        clearAllMocks()
    }

    given("사용자가 초대 코드로 스터디 그룹을 조회할 때") {

        `when`("유효한 초대 코드를 입력하면") {
            // 테스트용 객체를 when 블록 내부에서 생성
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )
            val testOwner = Member(
                id = ownerId,
                nickname = "Owner Nickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0")
            )

            every { loadStudyGroupPort.loadByInviteCode("TESTCODE") } returns testStudyGroup
            every { loadMemberPort.loadById(testStudyGroup.ownerId) } returns testOwner
            every { studyGroupMemberRepository.countByGroupId(testStudyGroup.id) } returns 5

            val result = studyGroupQueryService.getStudyGroupByInviteCode(memberInfo, "TESTCODE")

            then("해당 스터디 그룹의 요약 정보를 반환한다") {
                result.id shouldBe groupId.toHexString()
                result.name shouldBe "Test Study Group"
                result.description shouldBe "Test Description"
                result.membersCount shouldBe 5
                result.owner.nickname shouldBe "Owner Nickname"
                result.inviteCode.code shouldBe "TESTCODE"
            }
        }

        `when`("존재하지 않는 초대 코드를 입력하면") {
            every { loadStudyGroupPort.loadByInviteCode("INVALID") } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupQueryService.getStudyGroupByInviteCode(memberInfo, "INVALID")
                }
                exception.message shouldBe "해당 초대 코드를 사용하는 스터디 그룹이 존재하지 않습니다."
            }
        }

        `when`("스터디 그룹은 존재하지만 소유자 정보가 없으면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )

            every { loadStudyGroupPort.loadByInviteCode("TESTCODE") } returns testStudyGroup
            every { loadMemberPort.loadById(testStudyGroup.ownerId) } returns null
            every { studyGroupMemberRepository.countByGroupId(testStudyGroup.id) } returns 5

            val result = studyGroupQueryService.getStudyGroupByInviteCode(memberInfo, "TESTCODE")

            then("기본 소유자 정보로 응답한다") {
                result.owner.nickname shouldBe "Unknown"
                result.owner.profileImage.url shouldBe "0"
            }
        }
    }

    given("사용자가 자신이 가입한 스터디 그룹 목록을 조회할 때") {
        `when`("가입한 스터디 그룹이 존재하면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )
            val testOwner = Member(
                id = ownerId,
                nickname = "Owner Nickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0")
            )

            val groupIds = listOf(groupId.toHexString())
            every { studyGroupMemberRepository.findGroupIdsByMemberId(memberId) } returns groupIds
            every { loadStudyGroupPort.loadByGroupIds(groupIds) } returns listOf(testStudyGroup)
            every { loadMemberPort.loadById(testStudyGroup.ownerId) } returns testOwner

            val result = studyGroupQueryService.getMyStudyGroups(memberInfo)

            then("가입한 스터디 그룹 목록을 반환한다") {
                result shouldHaveSize 1
                result[0].id shouldBe groupId.toHexString()
                result[0].name shouldBe "Test Study Group"
            }
        }

        `when`("가입한 스터디 그룹이 없으면") {
            every { studyGroupMemberRepository.findGroupIdsByMemberId(memberId) } returns emptyList()

            val result = studyGroupQueryService.getMyStudyGroups(memberInfo)

            then("빈 목록을 반환한다") {
                result.shouldBeEmpty()
            }
        }

        `when`("가입한 스터디 그룹은 있지만 소유자 정보가 없으면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )

            val groupIds = listOf(groupId.toHexString())
            every { studyGroupMemberRepository.findGroupIdsByMemberId(memberId) } returns groupIds
            every { loadStudyGroupPort.loadByGroupIds(groupIds) } returns listOf(testStudyGroup)
            every { loadMemberPort.loadById(testStudyGroup.ownerId) } returns null

            val result = studyGroupQueryService.getMyStudyGroups(memberInfo)

            then("기본 소유자 정보로 스터디 그룹 목록을 반환한다") {
                result[0].owner.nickname shouldBe "Unknown"
            }
        }
    }

    given("사용자가 스터디 그룹의 초대 코드를 조회할 때") {
        `when`("유효한 초대 코드가 있고 그룹 멤버라면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )
            val testStudyGroupMember = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = "testNickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.MEMBER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns testStudyGroup
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(testStudyGroup.id, testStudyGroupMember.memberId) } returns testStudyGroupMember

            val result = studyGroupQueryService.getInviteCode(memberInfo, groupId.toHexString())

            then("기존 초대 코드를 반환한다") {
                result.code shouldBe "TESTCODE"
                result.createdAt shouldNotBe null
                result.expireAt shouldNotBe null
            }
        }

        `when`("초대 코드가 만료되었고 그룹 멤버라면") {
            val expiredInviteCode = InviteCodeVo(
                code = "EXPIRED",
                createdAt = LocalDateTime.now().minusDays(10),
                expireAt = LocalDateTime.now().minusDays(1)
            )
            val studyGroupWithExpiredCode = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                inviteCode = expiredInviteCode
            )
            val updatedStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                inviteCode = InviteCodeVo(code = "NEWCODE", createdAt = LocalDateTime.now(), expireAt = LocalDateTime.now().plusDays(7))
            )
            val testStudyGroupMember = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = "testNickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.MEMBER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroupWithExpiredCode
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(studyGroupWithExpiredCode.id, testStudyGroupMember.memberId) } returns testStudyGroupMember
            every { loadStudyGroupPort.existsByInviteCode(any()) } returns false
            every { saveStudyGroupPort.update(any()) } returns updatedStudyGroup

            val result = studyGroupQueryService.getInviteCode(memberInfo, groupId.toHexString())

            then("새로운 초대 코드를 생성하여 반환한다") {
                result.code shouldBe "NEWCODE"
            }
        }

        `when`("존재하지 않는 스터디 그룹의 초대 코드를 조회하면") {
            every { loadStudyGroupPort.loadByGroupId("invalid") } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupQueryService.getInviteCode(memberInfo, "invalid")
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("스터디 그룹에 가입하지 않은 사용자가 초대 코드를 조회하면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns testStudyGroup
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(testStudyGroup.id, ObjectId(memberInfo.memberId)) } returns null

            then("권한이 없다는 예외가 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyGroupQueryService.getInviteCode(memberInfo, groupId.toHexString())
                }
                exception.message shouldBe "해당 스터디 그룹에 가입되어 있지 않습니다."
            }
        }
    }

    given("사용자가 스터디 그룹 상세 정보를 조회할 때") {
        `when`("존재하는 스터디 그룹의 멤버가 조회하면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )
            val testOwner = Member(
                id = ownerId,
                nickname = "Owner Nickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0")
            )
            val testStudyGroupMember = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = "testNickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.MEMBER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns testStudyGroup
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(testStudyGroup.id, testStudyGroupMember.memberId) } returns testStudyGroupMember
            every { loadMemberPort.loadById(testStudyGroup.ownerId) } returns testOwner

            val result = studyGroupQueryService.getStudyGroup(memberInfo, groupId.toHexString())

            then("스터디 그룹 상세 정보를 반환한다") {
                result.id shouldBe groupId.toHexString()
                result.name shouldBe "Test Study Group"
                result.description shouldBe "Test Description"
                result.owner.nickname shouldBe "Owner Nickname"
            }
        }

        `when`("존재하지 않는 스터디 그룹을 조회하면") {
            every { loadStudyGroupPort.loadByGroupId("invalid") } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupQueryService.getStudyGroup(memberInfo, "invalid")
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("스터디 그룹에 가입하지 않은 사용자가 조회하면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns testStudyGroup
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(testStudyGroup.id, ObjectId(memberInfo.memberId)) } returns null

            then("권한이 없다는 예외가 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyGroupQueryService.getStudyGroup(memberInfo, groupId.toHexString())
                }
                exception.message shouldBe "해당 스터디 그룹에 가입되어 있지 않습니다."
            }
        }

        `when`("스터디 그룹은 존재하지만 소유자 정보가 없으면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )
            val testStudyGroupMember = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = "testNickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.MEMBER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns testStudyGroup
            every { studyGroupMemberRepository.findByGroupIdAndMemberId(testStudyGroup.id, testStudyGroupMember.memberId) } returns testStudyGroupMember
            every { loadMemberPort.loadById(testStudyGroup.ownerId) } returns null

            val result = studyGroupQueryService.getStudyGroup(memberInfo, groupId.toHexString())

            then("기본 소유자 정보로 스터디 그룹 정보를 반환한다") {
                result.owner.nickname shouldBe "Unknown"
            }
        }
    }

    given("사용자가 스터디 그룹의 멤버 목록을 조회할 때") {
        `when`("존재하는 스터디 그룹의 멤버가 조회하면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )
            val testStudyGroupMember = StudyGroupMember(
                groupId = groupId,
                memberId = memberId,
                nickname = "testNickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.MEMBER
            )

            val memberList = listOf(testStudyGroupMember)
            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns testStudyGroup
            every { studyGroupMemberRepository.findByGroupId(testStudyGroup.id) } returns memberList

            val result = studyGroupQueryService.getStudyGroupMembers(memberInfo, groupId.toHexString())

            then("그룹 멤버 목록을 반환한다") {
                result shouldHaveSize 1
            }
        }

        `when`("존재하지 않는 스터디 그룹의 멤버 목록을 조회하면") {
            every { loadStudyGroupPort.loadByGroupId("invalid") } returns null

            then("스터디 그룹을 찾을 수 없다는 예외가 발생한다") {
                val exception = shouldThrow<NotFoundException> {
                    studyGroupQueryService.getStudyGroupMembers(memberInfo, "invalid")
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("스터디 그룹에 가입하지 않은 사용자가 멤버 목록을 조회하면") {
            val testInviteCode = InviteCodeVo(
                code = "TESTCODE",
                createdAt = LocalDateTime.now().minusDays(1),
                expireAt = LocalDateTime.now().plusDays(7)
            )
            val testStudyGroup = StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "Test Study Group",
                description = "Test Description",
                memberCount = 5,
                profileImage = GroupProfileImageVo(type = "preset", url = "0"),
                inviteCode = testInviteCode
            )

            val otherMemberId = ObjectId.get()
            val otherMember = StudyGroupMember(
                groupId = groupId,
                memberId = otherMemberId,
                nickname = "otherNickname",
                profileImage = MemberProfileImageVo(type = "preset", url = "0"),
                role = GroupMemberRole.MEMBER
            )
            every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns testStudyGroup
            every { studyGroupMemberRepository.findByGroupId(testStudyGroup.id) } returns listOf(otherMember)

            then("권한이 없다는 예외가 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyGroupQueryService.getStudyGroupMembers(memberInfo, groupId.toHexString())
                }
                exception.message shouldBe "해당 스터디 그룹에 가입되어 있지 않습니다."
            }
        }
    }
})
