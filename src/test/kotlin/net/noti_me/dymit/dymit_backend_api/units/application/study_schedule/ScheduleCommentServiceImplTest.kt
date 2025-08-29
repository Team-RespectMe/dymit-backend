package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.CreateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.UpdateScheduleCommentCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.impl.ScheduleCommentServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId

class ScheduleCommentServiceImplTest : BehaviorSpec({

    val scheduleCommentRepository = mockk<ScheduleCommentRepository>()
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()
    val loadMemberPort = mockk<LoadMemberPort>()
    val studyScheduleRepository = mockk<StudyScheduleRepository>()

    val scheduleCommentService = ScheduleCommentServiceImpl(
        scheduleCommentRepository = scheduleCommentRepository,
        studyGroupMemberRepository = studyGroupMemberRepository,
        loadMemberPort = loadMemberPort,
        studyScheduleRepository = studyScheduleRepository
    )

    // Helper methods (defined before they are used)
    fun createTestMemberInfo(memberId: String = ObjectId().toHexString()): MemberInfo {
        return MemberInfo(
            memberId = memberId,
            nickname = "테스트사용자",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    fun createTestMember(id: ObjectId = ObjectId()): Member {
        return Member(
            id = id,
            nickname = "테스트사용자",
            profileImage = MemberProfileImageVo(
                type = "URL",
                filePath = "",
                url = "https://example.com/profile.jpg",
                fileSize = 0L,
                width = 100,
                height = 100
            )
        )
    }

    fun createTestStudyGroupMember(
        groupId: ObjectId = ObjectId(),
        memberId: ObjectId = ObjectId()
    ): StudyGroupMember {
        return StudyGroupMember(
            id = memberId, // 수정: ObjectId() → memberId로 변경
            groupId = groupId,
            memberId = memberId,
            nickname = "테스트사용자",
            profileImage = MemberProfileImageVo(
                type = "URL",
                filePath = "",
                url = "https://example.com/profile.jpg",
                fileSize = 0L,
                width = 100,
                height = 100
            ),
            role = GroupMemberRole.MEMBER
        )
    }

    fun createTestStudySchedule(
        id: ObjectId = ObjectId(),
        groupId: ObjectId = ObjectId()
    ): StudySchedule {
        return StudySchedule(
            id = id,
            groupId = groupId,
            title = "테스트 스케줄",
            description = "테스트 스케줄 설명",
            scheduleAt = java.time.LocalDateTime.now().plusDays(1)
        )
    }

    fun createTestScheduleComment(
        id: ObjectId = ObjectId(),
        scheduleId: ObjectId = ObjectId(),
        writerId: ObjectId = ObjectId()
    ): ScheduleComment {
        val writer = Writer(
            id = writerId,
            nickname = "테스트작성자",
            image = ProfileImageVo(
                type = "URL",
                url = "https://example.com/profile.jpg"
            )
        )

        return ScheduleComment(
            id = id,
            scheduleId = scheduleId,
            writer = writer,
            content = "테스트 댓글 내용"
        )
    }

    // 공통 테스트 데이터
    lateinit var testMemberId: ObjectId
    lateinit var testGroupId: ObjectId
    lateinit var testScheduleId: ObjectId
    lateinit var testCommentId: ObjectId
    lateinit var testMemberInfo: MemberInfo
    lateinit var testMember: Member
    lateinit var testStudyGroupMember: StudyGroupMember
    lateinit var testStudySchedule: StudySchedule
    lateinit var testScheduleComment: ScheduleComment

    beforeEach {
        clearAllMocks()

        testMemberId = ObjectId()
        testGroupId = ObjectId()
        testScheduleId = ObjectId()
        testCommentId = ObjectId()

        testMemberInfo = createTestMemberInfo(testMemberId.toHexString())
        testMember = createTestMember(testMemberId)
        testStudyGroupMember = createTestStudyGroupMember(testGroupId, testMemberId)
        testStudySchedule = createTestStudySchedule(testScheduleId, testGroupId)
        testScheduleComment = createTestScheduleComment(testCommentId, testScheduleId, testMemberId)
    }

    Given("댓글 생성 요청이 주어졌을 때") {
        When("정상적인 요청인 경우") {
            Then("댓글이 성공적으로 생성되어야 한다") {
                // given
                val command = CreateScheduleCommentCommand(
                    groupId = testGroupId,
                    scheduleId = testScheduleId,
                    content = "테스트 댓글"
                )

                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testStudyGroupMember
                every { studyScheduleRepository.loadById(testScheduleId) } returns testStudySchedule
                every { loadMemberPort.loadById(testMemberId) } returns testMember
                every { scheduleCommentRepository.save(any()) } returns testScheduleComment

                // when
                val result = scheduleCommentService.createComment(testMemberInfo, command)

                // then
                result shouldNotBe null
                result.content shouldBe testScheduleComment.content
                verify { scheduleCommentRepository.save(any()) }
            }
        }

        When("그룹 멤버가 아닌 경우") {
            Then("ForbiddenException이 발생해야 한다") {
                // given
                val command = CreateScheduleCommentCommand(
                    groupId = testGroupId,
                    scheduleId = testScheduleId,
                    content = "테스트 댓글"
                )

                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns null

                // when & then
                shouldThrow<ForbiddenException> {
                    scheduleCommentService.createComment(testMemberInfo, command)
                }
            }
        }

        When("스케줄이 존재하지 않는 경우") {
            Then("NotFoundException이 발생해야 한다") {
                // given
                val command = CreateScheduleCommentCommand(
                    groupId = testGroupId,
                    scheduleId = testScheduleId,
                    content = "테스트 댓글"
                )

                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testStudyGroupMember
                every { studyScheduleRepository.loadById(testScheduleId) } returns null

                // when & then
                shouldThrow<NotFoundException> {
                    scheduleCommentService.createComment(testMemberInfo, command)
                }
            }
        }

        When("멤버 정보를 찾을 수 없는 경우") {
            Then("NotFoundException이 발생해야 한다") {
                // given
                val command = CreateScheduleCommentCommand(
                    groupId = testGroupId,
                    scheduleId = testScheduleId,
                    content = "테스트 댓글"
                )

                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testStudyGroupMember
                every { studyScheduleRepository.loadById(testScheduleId) } returns testStudySchedule
                every { loadMemberPort.loadById(testMemberId) } returns null

                // when & then
                shouldThrow<NotFoundException> {
                    scheduleCommentService.createComment(testMemberInfo, command)
                }
            }
        }
    }

    Given("댓글 수정 요청이 주어졌을 때") {
        When("정상적인 요청인 경우") {
            Then("댓글이 성공적으로 수정되어야 한다") {
                // given
                val command = UpdateScheduleCommentCommand(
                    groupId = testGroupId,
                    scheduleId = testScheduleId,
                    commentId = testCommentId,
                    content = "수정된 댓글 내용"
                )

                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testStudyGroupMember
                every { scheduleCommentRepository.findById(testCommentId) } returns testScheduleComment
                every { scheduleCommentRepository.save(any()) } returns testScheduleComment

                // when
                val result = scheduleCommentService.updateComment(testMemberInfo, command)

                // then
                result shouldNotBe null
                verify { scheduleCommentRepository.save(any()) }
            }
        }

        When("그룹 멤버가 아닌 경우") {
            Then("ForbiddenException이 발생해야 한다") {
                // given
                val command = UpdateScheduleCommentCommand(
                    groupId = testGroupId,
                    scheduleId = testScheduleId,
                    commentId = testCommentId,
                    content = "수정된 댓글 내용"
                )

                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns null

                // when & then
                shouldThrow<ForbiddenException> {
                    scheduleCommentService.updateComment(testMemberInfo, command)
                }
            }
        }

        When("댓글이 존재하지 않는 경우") {
            Then("NotFoundException이 발생해야 한다") {
                // given
                val command = UpdateScheduleCommentCommand(
                    groupId = testGroupId,
                    scheduleId = testScheduleId,
                    commentId = testCommentId,
                    content = "수정된 댓글 내용"
                )

                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testStudyGroupMember
                every { scheduleCommentRepository.findById(testCommentId) } returns null

                // when & then
                shouldThrow<NotFoundException> {
                    scheduleCommentService.updateComment(testMemberInfo, command)
                }
            }
        }
    }

    Given("댓글 삭제 요청이 주어졌을 때") {
        When("정상적인 요청인 경우") {
            Then("댓글이 성공적으로 삭제되어야 한다") {
                // given
                every { scheduleCommentRepository.findById(testCommentId) } returns testScheduleComment
                every { scheduleCommentRepository.deleteById(testCommentId) } returns Unit

                // when
                scheduleCommentService.deleteComment(testMemberInfo, testCommentId.toHexString())

                // then
                verify { scheduleCommentRepository.deleteById(testCommentId) }
            }
        }

        When("댓글이 존재하지 않는 경우") {
            Then("NotFoundException이 발생해야 한다") {
                // given
                every { scheduleCommentRepository.findById(testCommentId) } returns null

                // when & then
                shouldThrow<NotFoundException> {
                    scheduleCommentService.deleteComment(testMemberInfo, testCommentId.toHexString())
                }
            }
        }

        When("댓글 작성자가 아닌 경우") {
            Then("ForbiddenException이 발생해야 한다") {
                // given
                val otherMemberId = ObjectId()
                val otherComment = createTestScheduleComment(testCommentId, testScheduleId, otherMemberId)

                every { scheduleCommentRepository.findById(testCommentId) } returns otherComment

                // when & then
                shouldThrow<ForbiddenException> {
                    scheduleCommentService.deleteComment(testMemberInfo, testCommentId.toHexString())
                }
            }
        }
    }

    Given("댓글 목록 조회 요청이 주어졌을 때") {
        When("정상적인 요청인 경우") {
            Then("댓글 목록이 성공적으로 조회되어야 한다") {
                // given
                val comments = listOf(testScheduleComment)
                val cursor = ObjectId()

                every { studyScheduleRepository.loadById(testScheduleId) } returns testStudySchedule
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testStudyGroupMember
                every { scheduleCommentRepository.findByScheduleId(testScheduleId, cursor, 10L) } returns comments

                // when
                val result = scheduleCommentService.getScheduleComments(
                    testMemberInfo,
                    testScheduleId.toHexString(),
                    cursor.toHexString(),
                    10
                )

                // then
                result shouldNotBe null
                result.size shouldBe 1
                result[0].content shouldBe testScheduleComment.content
            }
        }

        When("스케줄이 존재하지 않는 경우") {
            Then("NotFoundException이 발생해야 한다") {
                // given
                every { studyScheduleRepository.loadById(testScheduleId) } returns null

                // when & then
                shouldThrow<NotFoundException> {
                    scheduleCommentService.getScheduleComments(
                        testMemberInfo,
                        testScheduleId.toHexString(),
                        null,
                        10
                    )
                }
            }
        }

        When("그룹 멤버가 아닌 경우") {
            Then("ForbiddenException이 발생해야 한다") {
                // given
                every { studyScheduleRepository.loadById(testScheduleId) } returns testStudySchedule
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns null

                // when & then
                shouldThrow<ForbiddenException> {
                    scheduleCommentService.getScheduleComments(
                        testMemberInfo,
                        testScheduleId.toHexString(),
                        null,
                        10
                    )
                }
            }
        }
    }
})
