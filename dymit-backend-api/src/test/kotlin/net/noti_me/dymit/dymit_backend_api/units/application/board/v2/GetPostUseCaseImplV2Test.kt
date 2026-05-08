package net.noti_me.dymit.dymit_backend_api.units.application.board.v2

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.application.board.v2.impl.GetPostUseCaseImplV2
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
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.BoardRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.v2.PostRepositoryV2
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.supports.createProfileImageVo
import org.bson.types.ObjectId

internal class GetPostUseCaseImplV2Test : BehaviorSpec({

    val postRepository = mockk<PostRepositoryV2>()
    val boardRepository = mockk<BoardRepositoryV2>()
    val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()

    val useCase = GetPostUseCaseImplV2(
        postRepository = postRepository,
        boardRepository = boardRepository,
        groupMemberRepository = studyGroupMemberRepository
    )

    val groupId = ObjectId.get()
    val boardId = ObjectId.get()
    val memberId = ObjectId.get()
    val postId = ObjectId.get()

    fun memberInfo(): MemberInfo {
        return MemberInfo(
            memberId = memberId.toHexString(),
            nickname = "member",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    fun createGroupMember(role: GroupMemberRole = GroupMemberRole.MEMBER): StudyGroupMember {
        return StudyGroupMember(
            id = ObjectId.get(),
            groupId = groupId,
            memberId = memberId,
            nickname = "nickname",
            profileImage = createProfileImageVo(),
            role = role
        )
    }

    fun createBoard(canReadMember: Boolean = true): Board {
        val memberActions = if (canReadMember) {
            mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST)
        } else {
            mutableListOf(BoardAction.WRITE_POST)
        }
        return Board(
            id = boardId,
            groupId = groupId,
            name = "게시판",
            permissions = mutableSetOf(
                BoardPermission(
                    role = GroupMemberRole.OWNER,
                    actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.READ_POST, BoardAction.WRITE_POST)
                ),
                BoardPermission(
                    role = GroupMemberRole.MEMBER,
                    actions = memberActions
                )
            )
        )
    }

    fun createPost(writerMember: StudyGroupMember, targetBoardId: ObjectId = boardId): Post {
        return Post(
            id = postId,
            groupId = groupId,
            boardId = targetBoardId,
            writer = Writer.from(writerMember),
            title = "title",
            content = "content",
            category = PostCategory.QUESTION
        )
    }

    beforeEach {
        clearAllMocks()
    }

    given("게시글 V2 단건 조회 시") {
        `when`("정상 요청이면") {
            then("게시글을 반환한다") {
                val member = createGroupMember()
                val post = createPost(member)
                every { postRepository.findById(postId.toHexString()) } returns post
                every { boardRepository.findById(boardId) } returns createBoard(true)
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member

                val result = useCase.get(
                    memberInfo = memberInfo(),
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    postId = postId.toHexString()
                )
                result.id shouldBe post.identifier
            }
        }

        `when`("게시글이 없으면") {
            then("리소스 예외가 발생한다") {
                every { postRepository.findById(postId.toHexString()) } returns null

                shouldThrow<NotFoundException> {
                    useCase.get(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }

        `when`("게시판이 없으면") {
            then("리소스 예외가 발생한다") {
                val member = createGroupMember()
                every { postRepository.findById(postId.toHexString()) } returns createPost(member)
                every { boardRepository.findById(boardId) } returns null

                shouldThrow<NotFoundException> {
                    useCase.get(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }

        `when`("요청자가 그룹 멤버가 아니면") {
            then("리소스 예외가 발생한다") {
                val member = createGroupMember()
                every { postRepository.findById(postId.toHexString()) } returns createPost(member)
                every { boardRepository.findById(boardId) } returns createBoard(true)
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns null

                shouldThrow<NotFoundException> {
                    useCase.get(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }

        `when`("요청 게시판과 게시글의 소속이 다르면") {
            then("리소스 예외가 발생한다") {
                val member = createGroupMember()
                every { postRepository.findById(postId.toHexString()) } returns createPost(member, ObjectId.get())
                every { boardRepository.findById(any<ObjectId>()) } returns createBoard(true)
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member

                shouldThrow<NotFoundException> {
                    useCase.get(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }

        `when`("게시판 읽기 권한이 없으면") {
            then("권한 예외가 발생한다") {
                val member = createGroupMember()
                every { postRepository.findById(postId.toHexString()) } returns createPost(member)
                every { boardRepository.findById(boardId) } returns createBoard(false)
                every { studyGroupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns member

                shouldThrow<NotFoundException> {
                    useCase.get(
                        memberInfo = memberInfo(),
                        groupId = groupId.toHexString(),
                        boardId = boardId.toHexString(),
                        postId = postId.toHexString()
                    )
                }
            }
        }
    }
})
