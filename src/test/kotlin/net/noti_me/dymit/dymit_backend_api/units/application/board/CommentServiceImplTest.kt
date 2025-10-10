package net.noti_me.dymit.dymit_backend_api.units.application.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.board.dto.CommentCommand
import net.noti_me.dymit.dymit_backend_api.application.board.impl.CommentServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.CommentRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId

class CommentServiceImplTest : BehaviorSpec({

    val boardRepository = mockk<BoardRepository>()
    val commentRepository = mockk<CommentRepository>()
    val groupMemberRepository = mockk<StudyGroupMemberRepository>()

    val commentService = CommentServiceImpl(
        groupMemberRepository = groupMemberRepository,
        boardRepository = boardRepository,
        commentRepository = commentRepository
    )

    // Test data setup helpers
    fun createTestMemberInfo(memberId: String = ObjectId().toHexString()): MemberInfo {
        return MemberInfo(
            memberId = memberId,
            nickname = "테스트사용자",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    fun createTestCommentCommand(
        groupId: String = ObjectId().toHexString(),
        boardId: String = ObjectId().toHexString(),
        postId: String = ObjectId().toHexString(),
        content: String = "테스트 댓글 내용"
    ): CommentCommand {
        return CommentCommand(
            groupId = groupId,
            boardId = boardId,
            postId = postId,
            content = content
        )
    }

    fun createTestBoard(
        id: ObjectId = ObjectId(),
        groupId: ObjectId = ObjectId(),
        name: String = "테스트 게시판",
        hasWriteCommentPermission: Boolean = true,
        memberRole: GroupMemberRole = GroupMemberRole.MEMBER
    ): Board {
        val permissions = if (hasWriteCommentPermission) {
            mutableSetOf(
                BoardPermission(
                    role = memberRole,
                    actions = mutableListOf(BoardAction.WRITE_COMMENT, BoardAction.READ_COMMENT)
                )
            )
        } else {
            mutableSetOf(
                BoardPermission(
                    role = memberRole,
                    actions = mutableListOf(BoardAction.READ_COMMENT)
                )
            )
        }

        return Board(
            id = id,
            groupId = groupId,
            name = name,
            permissions = permissions
        )
    }

    fun createTestStudyGroupMember(
        id: ObjectId = ObjectId(),
        groupId: ObjectId = ObjectId(),
        memberId: ObjectId = ObjectId(),
        nickname: String = "테스트닉네임",
        role: GroupMemberRole = GroupMemberRole.MEMBER
    ): StudyGroupMember {
        return StudyGroupMember(
            id = id,
            groupId = groupId,
            memberId = memberId,
            nickname = nickname,
            profileImage = ProfileImageVo(
                url = "https://example.com/profile.jpg",
                type = "url"
            ),
            role = role
        )
    }

    beforeEach {
        clearAllMocks()
    }

    given("댓글 생성 요청이 들어올 때") {

        `when`("정상적인 요청인 경우") {
            then("댓글이 성공적으로 생성되어야 한다") {
                // Given
                val memberId = ObjectId()
                val groupId = ObjectId()
                val boardId = ObjectId()
                val postId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString(),
                    postId = postId.toHexString(),
                    content = "새로운 댓글입니다"
                )

                val testBoard = createTestBoard(
                    id = boardId,
                    groupId = groupId,
                    hasWriteCommentPermission = true,
                    memberRole = GroupMemberRole.MEMBER
                )

                val testGroupMember = createTestStudyGroupMember(
                    groupId = groupId,
                    memberId = memberId,
                    nickname = "댓글작성자",
                    role = GroupMemberRole.MEMBER
                )

                val savedComment = PostComment(
                    postId = postId,
                    writer = Writer(
                        id = testGroupMember.memberId,
                        nickname = testGroupMember.nickname,
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = testGroupMember.profileImage.url,
                            type = testGroupMember.profileImage.type
                        )
                    ),
                    content = command.content
                )

                every { boardRepository.findById(boardId) } returns testBoard
                every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns testGroupMember
                every { commentRepository.save(any<PostComment>()) } returns savedComment

                // When
                val result = commentService.createComment(memberInfo, command)

                // Then
                result shouldNotBe null
                result.content shouldBe "새로운 댓글입니다"
                result.writer.nickname shouldBe "댓글작성자"

                verify(exactly = 1) { boardRepository.findById(boardId) }
                verify(exactly = 1) { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) }
                verify(exactly = 1) { commentRepository.save(any<PostComment>()) }
            }
        }

        `when`("존재하지 않는 게시판에 댓글을 작성하려는 경우") {
            then("NotFoundException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val groupId = ObjectId()
                val boardId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString()
                )

                every { boardRepository.findById(boardId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    commentService.createComment(memberInfo, command)
                }

                exception.message shouldBe "해당 게시판을 찾을 수 없습니다."

                verify(exactly = 1) { boardRepository.findById(boardId) }
                verify(exactly = 0) { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) }
                verify(exactly = 0) { commentRepository.save(any<PostComment>()) }
            }
        }

        `when`("그룹 멤버가 아닌 사용자가 댓글을 작성하려는 경우") {
            then("ForbiddenException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val groupId = ObjectId()
                val boardId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString()
                )

                val testBoard = createTestBoard(
                    id = boardId,
                    groupId = groupId
                )

                every { boardRepository.findById(boardId) } returns testBoard
                every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns null

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    commentService.createComment(memberInfo, command)
                }

                exception.message shouldBe "해당 그룹의 멤버가 아닙니다."

                verify(exactly = 1) { boardRepository.findById(boardId) }
                verify(exactly = 1) { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) }
                verify(exactly = 0) { commentRepository.save(any<PostComment>()) }
            }
        }

        `when`("댓글 작성 권한이 없는 멤버가 댓글을 작성하려는 경우") {
            then("ForbiddenException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val groupId = ObjectId()
                val boardId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    boardId = boardId.toHexString()
                )

                val testBoard = createTestBoard(
                    id = boardId,
                    groupId = groupId,
                    hasWriteCommentPermission = false,
                    memberRole = GroupMemberRole.MEMBER
                )

                val testGroupMember = createTestStudyGroupMember(
                    groupId = groupId,
                    memberId = memberId,
                    role = GroupMemberRole.MEMBER
                )

                every { boardRepository.findById(boardId) } returns testBoard
                every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns testGroupMember

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    commentService.createComment(memberInfo, command)
                }

                exception.message shouldBe "해당 게시판에 댓글 작성 권한이 없습니다."

                verify(exactly = 1) { boardRepository.findById(boardId) }
                verify(exactly = 1) { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) }
                verify(exactly = 0) { commentRepository.save(any<PostComment>()) }
            }
        }
    }

    given("댓글 수정 요청이 들어올 때") {

        `when`("정상적인 수정 요청인 경우") {
            then("댓글이 성공적으로 수정되어야 한다") {
                // Given
                val memberId = ObjectId()
                println("memberId: $memberId")
                val groupId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                println("memberInfo: ${memberInfo.memberId}")
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    content = "수정된 댓글 내용"
                )

                val testGroupMember = createTestStudyGroupMember(
                    groupId = groupId,
                    memberId = memberId,
                    nickname = "댓글작성자",
                    role = GroupMemberRole.MEMBER
                )
                println("testGroupMember: ${testGroupMember.memberId}")

                val originalComment = PostComment(
                    postId = ObjectId(),
                    writer = Writer(
                        id = testGroupMember.memberId,
                        nickname = testGroupMember.nickname,
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = testGroupMember.profileImage.url,
                            type = testGroupMember.profileImage.type
                        )
                    ),
                    content = "원본 댓글 내용"
                )

                val updatedComment = PostComment(
                    postId = originalComment.postId,
                    writer = originalComment.writer,
                    content = "수정된 댓글 내용"
                )

                every { commentRepository.findById(commentId) } returns originalComment
                every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns testGroupMember
                every { commentRepository.save(any<PostComment>()) } returns updatedComment

                // When
                val result = commentService.updateComment(memberInfo, commentId, command)

                // Then
                result shouldNotBe null
                result.content shouldBe "수정된 댓글 내용"
                result.writer.nickname shouldBe "댓글작성자"

                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 1) { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) }
                verify(exactly = 1) { commentRepository.save(any<PostComment>()) }
            }
        }

        `when`("존재하지 않는 댓글을 수정하려는 경우") {
            then("NotFoundException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val groupId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    content = "수정된 댓글 내용"
                )

                every { commentRepository.findById(commentId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    commentService.updateComment(memberInfo, commentId, command)
                }

                exception.message shouldBe "해당 댓글을 찾을 수 없습니다."

                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 0) { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) }
                verify(exactly = 0) { commentRepository.save(any<PostComment>()) }
            }
        }

        `when`("그룹 멤버가 아닌 사용자가 댓글을 수정하려는 경우") {
            then("ForbiddenException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val groupId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    content = "수정된 댓글 내용"
                )

                val originalComment = PostComment(
                    postId = ObjectId(),
                    writer = Writer(
                        id = ObjectId(),
                        nickname = "다른사용자",
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = "https://example.com/profile.jpg",
                            type = "url"
                        )
                    ),
                    content = "원본 댓글 내용"
                )

                every { commentRepository.findById(commentId) } returns originalComment
                every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns null

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    commentService.updateComment(memberInfo, commentId, command)
                }

                exception.message shouldBe "해당 그룹의 멤버가 아닙니다."

                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 1) { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) }
                verify(exactly = 0) { commentRepository.save(any<PostComment>()) }
            }
        }

        `when`("다른 사용자의 댓글을 수정하려는 경우") {
            then("ForbiddenException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val groupId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())
                val command = createTestCommentCommand(
                    groupId = groupId.toHexString(),
                    content = "수정된 댓글 내용"
                )

                val testGroupMember = createTestStudyGroupMember(
                    groupId = groupId,
                    memberId = memberId,
                    nickname = "현재사용자",
                    role = GroupMemberRole.MEMBER
                )

                val originalComment = PostComment(
                    postId = ObjectId(),
                    writer = Writer(
                        id = ObjectId(),
                        nickname = "다른사용자",
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = "https://example.com/profile.jpg",
                            type = "url"
                        )
                    ),
                    content = "원본 댓글 내용"
                )

                every { commentRepository.findById(commentId) } returns originalComment
                every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns testGroupMember

                // When & Then
                shouldThrow<ForbiddenException> {
                    commentService.updateComment(memberInfo, commentId, command)
                }


                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 1) { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) }
                verify(exactly = 0) { commentRepository.save(any<PostComment>()) }
            }
        }
    }

    given("댓글 삭제 요청이 들어올 때") {

        `when`("정상적인 삭제 요청인 경우") {
            then("댓글이 성공적으로 삭제되어야 한다") {
                // Given
                val memberId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                val testComment = PostComment(
                    postId = ObjectId(),
                    writer = Writer(
                        id = memberId,
                        nickname = "댓글작성자",
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = "https://example.com/profile.jpg",
                            type = "url"
                        )
                    ),
                    content = "삭제할 댓글 내용"
                )

                every { commentRepository.findById(commentId) } returns testComment
                every { commentRepository.delete(testComment) } returns true

                // When
                commentService.removeComment(memberInfo, commentId)

                // Then
                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 1) { commentRepository.delete(testComment) }
            }
        }

        `when`("존재하지 않는 댓글을 삭제하려는 경우") {
            then("NotFoundException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                every { commentRepository.findById(commentId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    commentService.removeComment(memberInfo, commentId)
                }

                exception.message shouldBe "해당 댓글을 찾을 수 없습니다."

                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 0) { commentRepository.delete(any()) }
            }
        }

        `when`("다른 사용자의 댓글을 삭제하려는 경우") {
            then("ForbiddenException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val anotherMemberId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                val testComment = PostComment(
                    postId = ObjectId(),
                    writer = Writer(
                        id = anotherMemberId,
                        nickname = "다른사용자",
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = "https://example.com/profile.jpg",
                            type = "url"
                        )
                    ),
                    content = "다른 사용자의 댓글"
                )

                every { commentRepository.findById(commentId) } returns testComment

                // When & Then
                val exception = shouldThrow<ForbiddenException> {
                    commentService.removeComment(memberInfo, commentId)
                }

                exception.message shouldBe "본인의 댓글만 삭제할 수 있습니다."

                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 0) { commentRepository.delete(any()) }
            }
        }

        `when`("댓글 삭제에 실패하는 경우") {
            then("RuntimeException이 발생해야 한다") {
                // Given
                val memberId = ObjectId()
                val commentId = ObjectId().toHexString()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                val testComment = PostComment(
                    postId = ObjectId(),
                    writer = Writer(
                        id = memberId,
                        nickname = "댓글작성자",
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = "https://example.com/profile.jpg",
                            type = "url"
                        )
                    ),
                    content = "삭제할 댓글 내용"
                )

                every { commentRepository.findById(commentId) } returns testComment
                every { commentRepository.delete(testComment) } returns false

                // When & Then
                val exception = shouldThrow<RuntimeException> {
                    commentService.removeComment(memberInfo, commentId)
                }

                exception.message shouldBe "댓글 삭제에 실패했습니다."

                verify(exactly = 1) { commentRepository.findById(commentId) }
                verify(exactly = 1) { commentRepository.delete(testComment) }
            }
        }
    }

    given("댓글 조회 요청이 들어올 때") {

        `when`("정상적인 조회 요청인 경우") {
            then("게시물의 모든 댓글이 조회되어야 한다") {
                // Given
                val memberId = ObjectId()
                val postId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                val comment1 = PostComment(
                    postId = postId,
                    writer = Writer(
                        id = ObjectId(),
                        nickname = "작성자1",
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = "https://example.com/profile1.jpg",
                            type = "url"
                        )
                    ),
                    content = "첫 번째 댓글"
                )

                val comment2 = PostComment(
                    postId = postId,
                    writer = Writer(
                        id = ObjectId(),
                        nickname = "작성자2",
                        image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                            url = "https://example.com/profile2.jpg",
                            type = "url"
                        )
                    ),
                    content = "두 번째 댓글"
                )

                val comments = listOf(comment1, comment2)

                every { commentRepository.findByPostId(postId.toHexString()) } returns comments

                // When
                val result = commentService.getPostComments(memberInfo, postId.toHexString())

                // Then
                result shouldNotBe null
                result.size shouldBe 2
                result[0].content shouldBe "첫 번째 댓글"
                result[0].writer.nickname shouldBe "작성자1"
                result[1].content shouldBe "두 번째 댓글"
                result[1].writer.nickname shouldBe "작성자2"

                verify(exactly = 1) { commentRepository.findByPostId(postId.toHexString()) }
            }
        }

        `when`("댓글이 없는 게시물인 경우") {
            then("빈 리스트가 반환되어야 한다") {
                // Given
                val memberId = ObjectId()
                val postId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                every { commentRepository.findByPostId(postId.toHexString()) } returns emptyList()

                // When
                val result = commentService.getPostComments(memberInfo, postId.toHexString())

                // Then
                result shouldNotBe null
                result.size shouldBe 0

                verify(exactly = 1) { commentRepository.findByPostId(postId.toHexString()) }
            }
        }

        `when`("다량의 댓글이 있는 게시물인 경우") {
            then("모든 댓글이 올바르게 조회되어야 한다") {
                // Given
                val memberId = ObjectId()
                val postId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                val comments = (1..10).map { index ->
                    PostComment(
                        postId = postId,
                        writer = Writer(
                            id = ObjectId(),
                            nickname = "작성자$index",
                            image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                                url = "https://example.com/profile$index.jpg",
                                type = "url"
                            )
                        ),
                        content = "$index 번째 댓글입니다"
                    )
                }

                every { commentRepository.findByPostId(postId.toHexString()) } returns comments

                // When
                val result = commentService.getPostComments(memberInfo, postId.toHexString())

                // Then
                result shouldNotBe null
                result.size shouldBe 10

                // 몇 개의 샘플 검증
                result[0].content shouldBe "1 번째 댓글입니다"
                result[0].writer.nickname shouldBe "작성자1"
                result[4].content shouldBe "5 번째 댓글입니다"
                result[4].writer.nickname shouldBe "작성자5"
                result[9].content shouldBe "10 번째 댓글입니다"
                result[9].writer.nickname shouldBe "작성자10"

                verify(exactly = 1) { commentRepository.findByPostId(postId.toHexString()) }
            }
        }

        `when`("특정 사용자의 댓글만 있는 게시물인 경우") {
            then("해당 사용자의 모든 댓글이 조회되어야 한다") {
                // Given
                val memberId = ObjectId()
                val postId = ObjectId()
                val authorId = ObjectId()

                val memberInfo = createTestMemberInfo(memberId.toHexString())

                val comments = (1..3).map { index ->
                    PostComment(
                        postId = postId,
                        writer = Writer(
                            id = authorId,
                            nickname = "동일작성자",
                            image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(
                                url = "https://example.com/profile.jpg",
                                type = "url"
                            )
                        ),
                        content = "댓글 내용 $index"
                    )
                }

                every { commentRepository.findByPostId(postId.toHexString()) } returns comments

                // When
                val result = commentService.getPostComments(memberInfo, postId.toHexString())

                // Then
                result shouldNotBe null
                result.size shouldBe 3

                // 모든 댓글이 같은 작성자인지 확인
                result.forEach { comment ->
                    comment.writer.nickname shouldBe "동일작성자"
                }

                // 내용이 다른지 확인
                result[0].content shouldBe "댓글 내용 1"
                result[1].content shouldBe "댓글 내용 2"
                result[2].content shouldBe "댓글 내용 3"

                verify(exactly = 1) { commentRepository.findByPostId(postId.toHexString()) }
            }
        }
    }
})