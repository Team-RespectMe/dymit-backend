package net.noti_me.dymit.dymit_backend_api.units.application.board.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.impl.CreatePostUseCaseImplV2
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryPolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardCategoryWritePolicy
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.supports.createProfileImageVo
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher

internal class CreatePostUseCaseImplV2Test : BehaviorSpec({

    val postRepository = mockk<PostRepositoryV2>()
    val boardRepository = mockk<BoardRepositoryV2>()
    val loadStudyGroupPort = mockk<LoadStudyGroupPort>()
    val saveStudyGroupPort = mockk<SaveStudyGroupPort>(relaxed = true)
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()
    val scheduleParticipantRepository = mockk<ScheduleParticipantRepository>()
    val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    val useCase = CreatePostUseCaseImplV2(
        postRepository = postRepository,
        boardRepository = boardRepository,
        loadGroupPort = loadStudyGroupPort,
        saveGroupPort = saveStudyGroupPort,
        groupMemberRepository = studyGroupMemberRepository,
        scheduleParticipantRepository = scheduleParticipantRepository,
        eventPublisher = eventPublisher
    )

    val groupId = ObjectId.get()
    val boardId = ObjectId.get()
    val memberId = ObjectId.get()
    val scheduleId = ObjectId.get()
    val studyGroup = StudyGroup(
        id = groupId,
        ownerId = memberId,
        name = "그룹",
        description = "설명"
    )
    val board = Board(
        id = boardId,
        groupId = groupId,
        name = "게시판",
        permissions = mutableSetOf(
            BoardPermission(
                role = GroupMemberRole.OWNER,
                actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.READ_POST, BoardAction.WRITE_POST)
            ),
            BoardPermission(
                role = GroupMemberRole.ADMIN,
                actions = mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST)
            ),
            BoardPermission(
                role = GroupMemberRole.MEMBER,
                actions = mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST)
            )
        )
    )

    fun memberInfoFor(memberObjectId: ObjectId): MemberInfo {
        return MemberInfo(
            memberId = memberObjectId.toHexString(),
            nickname = "member",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    fun groupMember(role: GroupMemberRole, memberObjectId: ObjectId = memberId): StudyGroupMember {
        return StudyGroupMember(
            id = ObjectId.get(),
            groupId = groupId,
            memberId = memberObjectId,
            nickname = "닉네임",
            profileImage = createProfileImageVo(),
            role = role
        )
    }

    fun setupCommonMocks(groupMember: StudyGroupMember) {
        every { boardRepository.findById(boardId) } returns board
        every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup
        every {
            studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, groupMember.memberId)
        } returns groupMember
        every { saveStudyGroupPort.update(any()) } returns studyGroup
        justRun { eventPublisher.publishEvent(any()) }
        every { postRepository.save(any()) } answers {
            val source = firstArg<Post>()
            Post(
                id = ObjectId.get(),
                groupId = source.groupId,
                boardId = source.boardId,
                writer = source.writer,
                title = source.title,
                content = source.content,
                category = source.category,
                scheduleId = source.scheduleId
            )
        }
    }

    beforeEach {
        clearAllMocks()
    }

    given("게시글 V2 생성 시") {
        `when`("카테고리가 공지이고 요청자가 그룹 관리자이면") {
            then("게시글 생성에 성공한다") {
                val adminMember = groupMember(GroupMemberRole.ADMIN)
                setupCommonMocks(adminMember)
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "공지 제목",
                    content = "공지 내용",
                    category = PostCategory.NOTICE,
                    scheduleId = null
                )

                val result = useCase.create(memberInfoFor(adminMember.memberId), command)
                result.category shouldBe PostCategory.NOTICE
                result.scheduleId shouldBe null
            }
        }

        `when`("카테고리가 공지이고 요청자가 일반 멤버이면") {
            then("작성 권한 예외가 발생한다") {
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                setupCommonMocks(regularMember)
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "공지 제목",
                    content = "공지 내용",
                    category = PostCategory.NOTICE,
                    scheduleId = null
                )

                shouldThrow<ForbiddenException> {
                    useCase.create(memberInfoFor(regularMember.memberId), command)
                }
            }
        }

        `when`("카테고리가 회고이고 일정 참여자인 멤버가 요청하면") {
            then("게시글 생성에 성공한다") {
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                setupCommonMocks(regularMember)
                every {
                    scheduleParticipantRepository.existsByScheduleIdAndMemberId(scheduleId, regularMember.memberId)
                } returns true
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "회고 제목",
                    content = "회고 내용",
                    category = PostCategory.RETROSPECTIVE,
                    scheduleId = scheduleId.toHexString()
                )

                val result = useCase.create(memberInfoFor(regularMember.memberId), command)
                result.category shouldBe PostCategory.RETROSPECTIVE
                result.scheduleId shouldBe scheduleId.toHexString()
            }
        }

        `when`("카테고리가 회고이고 일정 비참여 멤버가 요청하면") {
            then("작성 권한 예외가 발생한다") {
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                setupCommonMocks(regularMember)
                every {
                    scheduleParticipantRepository.existsByScheduleIdAndMemberId(scheduleId, regularMember.memberId)
                } returns false
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "회고 제목",
                    content = "회고 내용",
                    category = PostCategory.RETROSPECTIVE,
                    scheduleId = scheduleId.toHexString()
                )

                shouldThrow<ForbiddenException> {
                    useCase.create(memberInfoFor(regularMember.memberId), command)
                }
            }
        }

        `when`("카테고리가 회고이고 게시판 정책이 전체 멤버 허용이어도 일정 비참여 멤버가 요청하면") {
            then("생성 V2에서는 참여자 검증으로 작성이 거부된다") {
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                val boardAllowAllForRetrospective = Board(
                    id = boardId,
                    groupId = groupId,
                    name = "게시판",
                    permissions = board.permissions,
                    categoryPolicies = mutableSetOf(
                        BoardCategoryPolicy(
                            category = PostCategory.NOTICE,
                            enabled = true,
                            writePolicy = BoardCategoryWritePolicy.GROUP_ADMIN_ONLY
                        ),
                        BoardCategoryPolicy(
                            category = PostCategory.RETROSPECTIVE,
                            enabled = true,
                            writePolicy = BoardCategoryWritePolicy.ALL_MEMBERS
                        ),
                        BoardCategoryPolicy(
                            category = PostCategory.QUESTION,
                            enabled = true,
                            writePolicy = BoardCategoryWritePolicy.ALL_MEMBERS
                        ),
                        BoardCategoryPolicy(
                            category = PostCategory.ASSIGNMENT,
                            enabled = true,
                            writePolicy = BoardCategoryWritePolicy.ALL_MEMBERS
                        )
                    )
                )
                every { boardRepository.findById(boardId) } returns boardAllowAllForRetrospective
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup
                every {
                    studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, regularMember.memberId)
                } returns regularMember
                every {
                    scheduleParticipantRepository.existsByScheduleIdAndMemberId(scheduleId, regularMember.memberId)
                } returns false
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "회고 제목",
                    content = "회고 내용",
                    category = PostCategory.RETROSPECTIVE,
                    scheduleId = scheduleId.toHexString()
                )

                shouldThrow<ForbiddenException> {
                    useCase.create(memberInfoFor(regularMember.memberId), command)
                }
            }
        }

        `when`("카테고리가 질문이고 일반 멤버가 요청하면") {
            then("게시글 생성에 성공한다") {
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                setupCommonMocks(regularMember)
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "질문 제목",
                    content = "질문 내용",
                    category = PostCategory.QUESTION,
                    scheduleId = null
                )

                val result = useCase.create(memberInfoFor(regularMember.memberId), command)
                result.category shouldBe PostCategory.QUESTION
            }
        }

        `when`("카테고리가 과제이고 일반 멤버가 요청하면") {
            then("게시글 생성에 성공한다") {
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                setupCommonMocks(regularMember)
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "과제 제목",
                    content = "과제 내용",
                    category = PostCategory.ASSIGNMENT,
                    scheduleId = null
                )

                val result = useCase.create(memberInfoFor(regularMember.memberId), command)
                result.category shouldBe PostCategory.ASSIGNMENT
            }
        }

        `when`("카테고리가 회고이고 scheduleId가 없으면") {
            then("입력 검증 예외가 발생한다") {
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                setupCommonMocks(regularMember)
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "회고 제목",
                    content = "회고 내용",
                    category = PostCategory.RETROSPECTIVE,
                    scheduleId = null
                )

                shouldThrow<BadRequestException> {
                    useCase.create(memberInfoFor(regularMember.memberId), command)
                }
            }
        }

        `when`("요청 그룹과 게시판 소속 그룹이 다르면") {
            then("리소스 예외가 발생한다") {
                val otherGroup = ObjectId.get()
                val regularMember = groupMember(GroupMemberRole.MEMBER)
                val boardInOtherGroup = Board(
                    id = boardId,
                    groupId = otherGroup,
                    name = "다른 그룹 게시판"
                )
                every { boardRepository.findById(boardId) } returns boardInOtherGroup
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns studyGroup
                every {
                    studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, regularMember.memberId)
                } returns regularMember

                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "제목",
                    content = "내용",
                    category = PostCategory.QUESTION,
                    scheduleId = null
                )

                shouldThrow<NotFoundException> {
                    useCase.create(memberInfoFor(regularMember.memberId), command)
                }
            }
        }
    }
})
