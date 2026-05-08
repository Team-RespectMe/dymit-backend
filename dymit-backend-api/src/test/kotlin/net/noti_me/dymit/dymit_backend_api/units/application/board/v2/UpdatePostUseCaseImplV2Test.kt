package net.noti_me.dymit.dymit_backend_api.units.application.board.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.board.v2.dto.PostCommandV2
import net.noti_me.dymit.dymit_backend_api.application.board.v2.impl.UpdatePostUseCaseImplV2
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.PostCategory
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
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
import java.time.LocalDateTime

internal class UpdatePostUseCaseImplV2Test : BehaviorSpec({

    val postRepository = mockk<PostRepositoryV2>()
    val boardRepository = mockk<BoardRepositoryV2>()
    val loadStudyGroupPort = mockk<LoadStudyGroupPort>()
    val saveStudyGroupPort = mockk<SaveStudyGroupPort>(relaxed = true)
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()
    val scheduleParticipantRepository = mockk<ScheduleParticipantRepository>()

    val useCase = UpdatePostUseCaseImplV2(
        postRepository = postRepository,
        boardRepository = boardRepository,
        loadGroupPort = loadStudyGroupPort,
        saveGroupPort = saveStudyGroupPort,
        groupMemberRepository = studyGroupMemberRepository,
        scheduleParticipantRepository = scheduleParticipantRepository
    )

    val groupId = ObjectId.get()
    val boardId = ObjectId.get()
    val memberId = ObjectId.get()
    val postId = ObjectId.get()
    val scheduleId = ObjectId.get()

    fun memberInfo(): MemberInfo {
        return MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "member",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    fun createGroupMember(role: GroupMemberRole): StudyGroupMember {
        return StudyGroupMember(
            id = ObjectId.get(),
            groupId = groupId,
            memberId = memberId,
            nickname = "nickname",
            profileImage = createProfileImageVo(),
            role = role
        )
    }

    fun createBoard(targetGroupId: ObjectId = groupId): Board {
        return Board(
            id = boardId,
            groupId = targetGroupId,
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
    }

    fun createPost(
        writerMember: StudyGroupMember,
        targetBoardId: ObjectId = boardId
    ): Post {
        return Post(
            id = postId,
            groupId = groupId,
            boardId = targetBoardId,
            writer = Writer.from(writerMember),
            title = "old-title",
            content = "old-content",
            category = PostCategory.QUESTION
        )
    }

    fun createStudyGroup(recentPostId: String?): StudyGroup {
        return StudyGroup(
            id = groupId,
            ownerId = memberId,
            name = "그룹",
            description = "설명",
            recentPost = recentPostId?.let {
                RecentPostVo(
                    postId = it,
                    title = "old-title",
                    createdAt = LocalDateTime.now()
                )
            }
        )
    }

    beforeEach {
        clearAllMocks()
    }

    given("게시글 V2 수정 시") {
        `when`("기본 카테고리로 정상 수정하면") {
            then("게시글을 수정하고 최근 게시글도 갱신한다") {
                val member = createGroupMember(GroupMemberRole.MEMBER)
                val board = createBoard()
                val post = createPost(member)
                val group = createStudyGroup(post.identifier)
                every { boardRepository.findById(boardId) } returns board
                every { postRepository.findById(postId.toHexString()) } returns post
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns group
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member
                every { postRepository.save(any()) } answers { firstArg() }
                every { saveStudyGroupPort.update(any()) } returns group
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "new-title",
                    content = "new-content",
                    category = PostCategory.QUESTION,
                    scheduleId = null
                )

                val result = useCase.update(memberInfo(), postId.toHexString(), command)

                result.title shouldBe "new-title"
                result.content shouldBe "new-content"
                verify(exactly = 1) { saveStudyGroupPort.update(any()) }
            }
        }

        `when`("최근 게시글과 무관한 게시글을 수정하면") {
            then("최근 게시글 갱신은 수행하지 않는다") {
                val member = createGroupMember(GroupMemberRole.MEMBER)
                val board = createBoard()
                val post = createPost(member)
                val group = createStudyGroup(ObjectId.get().toHexString())
                every { boardRepository.findById(boardId) } returns board
                every { postRepository.findById(postId.toHexString()) } returns post
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns group
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member
                every { postRepository.save(any()) } answers { firstArg() }
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "new-title",
                    content = "new-content",
                    category = PostCategory.QUESTION,
                    scheduleId = null
                )

                useCase.update(memberInfo(), postId.toHexString(), command)
                verify(exactly = 0) { saveStudyGroupPort.update(any()) }
            }
        }

        `when`("회고 카테고리로 수정하고 일정 비참여자이면") {
            then("권한 예외가 발생한다") {
                val member = createGroupMember(GroupMemberRole.MEMBER)
                val board = createBoard()
                val post = createPost(member)
                every { boardRepository.findById(boardId) } returns board
                every { postRepository.findById(postId.toHexString()) } returns post
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns createStudyGroup(post.identifier)
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member
                every { scheduleParticipantRepository.existsByScheduleIdAndMemberId(scheduleId, memberId) } returns false
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "retrospective-title",
                    content = "retrospective-content",
                    category = PostCategory.RETROSPECTIVE,
                    scheduleId = scheduleId.toHexString()
                )

                shouldThrow<ForbiddenException> {
                    useCase.update(memberInfo(), postId.toHexString(), command)
                }
            }
        }

        `when`("요청 그룹과 게시판 소속 그룹이 다르면") {
            then("리소스 예외가 발생한다") {
                val member = createGroupMember(GroupMemberRole.MEMBER)
                val post = createPost(member)
                every { boardRepository.findById(boardId) } returns createBoard(ObjectId.get())
                every { postRepository.findById(postId.toHexString()) } returns post
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns createStudyGroup(post.identifier)
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "new-title",
                    content = "new-content",
                    category = PostCategory.QUESTION,
                    scheduleId = null
                )

                shouldThrow<NotFoundException> {
                    useCase.update(memberInfo(), postId.toHexString(), command)
                }
            }
        }

        `when`("요청 게시판과 게시글의 소속이 다르면") {
            then("리소스 예외가 발생한다") {
                val member = createGroupMember(GroupMemberRole.MEMBER)
                every { boardRepository.findById(boardId) } returns createBoard()
                every { postRepository.findById(postId.toHexString()) } returns createPost(member, ObjectId.get())
                every { loadStudyGroupPort.loadByGroupId(groupId.toHexString()) } returns createStudyGroup(postId.toHexString())
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "new-title",
                    content = "new-content",
                    category = PostCategory.QUESTION,
                    scheduleId = null
                )

                shouldThrow<NotFoundException> {
                    useCase.update(memberInfo(), postId.toHexString(), command)
                }
            }
        }

        `when`("수정 대상 게시글이 없으면") {
            then("리소스 예외가 발생한다") {
                every { boardRepository.findById(boardId) } returns createBoard()
                every { postRepository.findById(postId.toHexString()) } returns null
                val command = PostCommandV2(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    title = "new-title",
                    content = "new-content",
                    category = PostCategory.QUESTION,
                    scheduleId = null
                )

                shouldThrow<NotFoundException> {
                    useCase.update(memberInfo(), postId.toHexString(), command)
                }
            }
        }
    }
})
