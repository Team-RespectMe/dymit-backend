package net.noti_me.dymit.dymit_backend_api.units.application.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.board.dto.PostCommand
import net.noti_me.dymit.dymit_backend_api.application.board.impl.PostServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.PostRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * PostServiceImpl에 대한 테스트 클래스
 * 게시글 생성, 수정, 삭제, 조회 기능을 테스트한다.
 */
class PostServiceImplTest : BehaviorSpec({

    // Mock 객체 선언
    val postRepository = mockk<PostRepository>()
    val loadGroupPort = mockk<LoadStudyGroupPort>()
    val saveGroupPort = mockk<SaveStudyGroupPort>()
    val boardRepository = mockk<BoardRepository>()
    val groupMemberRepository = mockk<StudyGroupMemberRepository>()
    val postService = PostServiceImpl(
        postRepository,
        loadGroupPort,
        saveGroupPort,
        boardRepository,
        groupMemberRepository
    )

    // 테스트용 데이터
    lateinit var memberInfo: MemberInfo
    lateinit var groupObjectId: ObjectId
    lateinit var memberObjectId: ObjectId
    lateinit var boardObjectId: ObjectId
    lateinit var postObjectId: ObjectId
    lateinit var groupMember: StudyGroupMember
    lateinit var studyGroup: StudyGroup
    lateinit var board: Board
    lateinit var post: Post
    lateinit var postCommand: PostCommand

    /**
     * 그룹 멤버 생성 헬퍼 메서드
     */
    fun createGroupMember(memberId: ObjectId, role: GroupMemberRole): StudyGroupMember {
        return StudyGroupMember(
            id = memberId,
            groupId = groupObjectId,
            memberId = memberId,
            nickname = "testUser",
            profileImage = MemberProfileImageVo(
                type = "presets",
                filePath = "/images/profile/default.jpg",
                url = "https://example.com/default.jpg",
                fileSize = 1024L,
                width = 200,
                height = 200
            ),
            role = role
        )
    }

    /**
     * 스터디 그룹 생성 헬퍼 메서드
     */
    fun createStudyGroup(): StudyGroup {
        return StudyGroup(
            id = groupObjectId,
            ownerId = memberObjectId,
            name = "테스트 그룹",
            description = "테스트 그룹 설명"
        )
    }

    /**
     * 게시판 생성 헬퍼 메서드
     */
    fun createBoard(): Board {
        val permissions = mutableSetOf(
            BoardPermission(
                role = GroupMemberRole.OWNER,
                actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.READ_POST, BoardAction.WRITE_POST)
            ),
            BoardPermission(
                role = GroupMemberRole.MEMBER,
                actions = mutableListOf(BoardAction.READ_POST, BoardAction.WRITE_POST)
            )
        )

        return Board(
            id = boardObjectId,
            groupId = groupObjectId,
            name = "테스트 게시판",
            permissions = permissions
        )
    }

    /**
     * 쓰기 권한이 없는 게시판 생성 헬퍼 메서드
     */
    fun createBoardWithoutWritePermission(): Board {
        val permissions = mutableSetOf(
            BoardPermission(
                role = GroupMemberRole.OWNER,
                actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.READ_POST, BoardAction.WRITE_POST)
            ),
            BoardPermission(
                role = GroupMemberRole.MEMBER,
                actions = mutableListOf(BoardAction.READ_POST)
            )
        )

        return Board(
            id = boardObjectId,
            groupId = groupObjectId,
            name = "테스트 게시판",
            permissions = permissions
        )
    }

    /**
     * 읽기 권한이 없는 게시판 생성 헬퍼 메서드
     */
    fun createBoardWithoutReadPermission(): Board {
        val permissions = mutableSetOf(
            BoardPermission(
                role = GroupMemberRole.OWNER,
                actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.READ_POST, BoardAction.WRITE_POST)
            )
        )

        return Board(
            id = boardObjectId,
            groupId = groupObjectId,
            name = "테스트 게시판",
            permissions = permissions
        )
    }

    /**
     * 게시글 생성 헬퍼 메서드
     */
    fun createPost(): Post {
        return Post(
            id = postObjectId,
            groupId = groupObjectId,
            boardId = boardObjectId,
            writer = Writer.from(groupMember),
            title = "테스트 제목",
            content = "테스트 내용"
        )
    }

    /**
     * 다른 최근 게시글을 가진 스터디 그룹 생성 헬퍼 메서드
     */
    fun createStudyGroupWithDifferentRecentPost(): StudyGroup {
        val differentPostId = ObjectId()
        return StudyGroup(
            id = groupObjectId,
            ownerId = memberObjectId,
            name = "테스트 그룹",
            description = "테스트 그룹 설명",
            recentPost = RecentPostVo(
                postId = differentPostId.toHexString(),
                title = "다른 게시글 제목",
                createdAt = LocalDateTime.now()
            )
        )
    }

    /**
     * 현재 게시글이 그룹의 최근 게시글인 경우를 위한 스터디 그룹 생성 헬퍼 메서드
     */
    fun createStudyGroupWithCurrentRecentPost(): StudyGroup {
        return StudyGroup(
            id = groupObjectId,
            ownerId = memberObjectId,
            name = "테스트 그룹",
            description = "테스트 그룹 설명",
            recentPost = RecentPostVo(
                postId = postObjectId.toHexString(),
                title = "테스트 제목",
                createdAt = LocalDateTime.now()
            )
        )
    }

    /**
     * 게시글 생성 성공 시나리오를 위한 Mock 설정
     */
    fun setupSuccessfulCreatePostMocks() {
        every { boardRepository.findById(boardObjectId) } returns board
        every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns studyGroup
        every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember
        every { postRepository.save(any<Post>()) } returns post
        every { saveGroupPort.persist(any()) } returns studyGroup
    }

    /**
     * 게시글 수정 성공 시나리오를 위한 Mock 설정
     */
    fun setupSuccessfulUpdatePostMocks() {
        every { boardRepository.findById(boardObjectId) } returns board
        every { postRepository.findById(postObjectId.toHexString()) } returns post
        every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns studyGroup
        every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember
        every { postRepository.save(any<Post>()) } returns post
        every { saveGroupPort.update(any()) } returns studyGroup
    }

    beforeContainer {

        // 공통 테스트 데이터 초기화
        groupObjectId = ObjectId()
        memberObjectId = ObjectId()
        boardObjectId = ObjectId()
        postObjectId = ObjectId()

        memberInfo = MemberInfo(
            memberId = memberObjectId.toHexString(),
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )

        groupMember = createGroupMember(memberObjectId, GroupMemberRole.MEMBER)
        studyGroup = createStudyGroup()
        board = createBoard()
        post = createPost()

        postCommand = PostCommand(
            groupId = groupObjectId.toHexString(),
            boardId = boardObjectId.toHexString(),
            title = "테스트 제목",
            content = "테스트 내용"
        )
    }

    Given("게시글을 생성할 때") {

        When("정상적인 요청으로 게시글을 생성하면") {

            Then("게시글이 성공적으로 생성되어야 한다") {
                // Given
                setupSuccessfulCreatePostMocks()

                // When
                val result = postService.createPost(memberInfo, postCommand)

                // Then
                result shouldNotBe null
                result.title shouldBe "테스트 제목"
                result.content shouldBe "테스트 내용"
                result.groupId shouldBe groupObjectId.toHexString()
                result.boardId shouldBe boardObjectId.toHexString()
                verify { postRepository.save(any<Post>()) }
                verify { saveGroupPort.persist(any()) }
            }
        }

        When("존재하지 않는 게시판에 게시글을 생성하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.createPost(memberInfo, postCommand)
                }
                exception.message shouldBe "해당 게시판을 찾을 수 없습니다."
            }
        }

        When("존재하지 않는 그룹에 게시글을 생성하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns board
                every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.createPost(memberInfo, postCommand)
                }
                exception.message shouldBe "해당 그룹을 찾을 수 없습니다."
            }
        }

        When("그룹의 멤버가 아닌 사용자가 게시글을 생성하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns board
                every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns studyGroup
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.createPost(memberInfo, postCommand)
                }
                exception.message shouldBe "해당 그룹의 멤버가 아닙니다."
            }
        }

        When("게시글 작성 권한이 없는 사용자가 게시글을 생성하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                val noWritePermissionBoard = createBoardWithoutWritePermission()
                every { boardRepository.findById(boardObjectId) } returns noWritePermissionBoard
                every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns studyGroup
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.createPost(memberInfo, postCommand)
                }
                exception.message shouldBe "해당 게시판에 글 작성 권한이 없습니다."
            }
        }
    }

    Given("게시글을 수정할 때") {

        When("정상적인 요청으로 게시글을 수정하면") {

            Then("게시글이 성공적으로 수정되어야 한다") {
                // Given
                setupSuccessfulUpdatePostMocks()

                // When
                val result = postService.updatePost(memberInfo, postObjectId.toHexString(), postCommand)

                // Then
                result shouldNotBe null
                result.title shouldBe "테스트 제목"
                result.content shouldBe "테스트 내용"
                verify { postRepository.save(any<Post>()) }
            }
        }

        When("존재하지 않는 게시판에 게시글을 수정하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.updatePost(memberInfo, postObjectId.toHexString(), postCommand)
                }
                exception.message shouldBe "해당 게시판을 찾을 수 없습니다."
            }
        }

        When("존재하지 않는 게시글을 수정하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns board
                every { postRepository.findById(postObjectId.toHexString()) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.updatePost(memberInfo, postObjectId.toHexString(), postCommand)
                }
                exception.message shouldBe "해당 게시글을 찾을 수 없습니다."
            }
        }

        When("존재하지 않는 그룹에 게시글을 수정하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns board
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.updatePost(memberInfo, postObjectId.toHexString(), postCommand)
                }
                exception.message shouldBe "해당 그룹을 찾을 수 없습니다."
            }
        }

        When("그룹의 멤버가 아닌 사용자가 게시글을 수정하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns board
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns studyGroup
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.updatePost(memberInfo, postObjectId.toHexString(), postCommand)
                }
                exception.message shouldBe "해당 그룹의 멤버가 아닙니다."
            }
        }

        When("최근 게시글이 아닌 게시글을 수정하면") {

            Then("최근 게시글 업데이트 없이 수정되어야 한다") {
                // Given
                val studyGroupWithDifferentRecentPost = createStudyGroupWithDifferentRecentPost()
                every { boardRepository.findById(boardObjectId) } returns board
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns studyGroupWithDifferentRecentPost
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember
                every { postRepository.save(any<Post>()) } returns post

                // When
                val result = postService.updatePost(memberInfo, postObjectId.toHexString(), postCommand)

                // Then
                result shouldNotBe null
                verify { postRepository.save(any<Post>()) }
                verify(exactly = 0) { saveGroupPort.update(any()) }
            }
        }

        When("현재 게시글이 그룹의 최근 게시글인 경우 수정하면") {

            Then("최근 게시글 정보도 함께 업데이트되어야 한다") {
                // Given
                val studyGroupWithCurrentRecentPost = createStudyGroupWithCurrentRecentPost()
                every { boardRepository.findById(boardObjectId) } returns board
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { loadGroupPort.loadByGroupId(groupObjectId.toHexString()) } returns studyGroupWithCurrentRecentPost
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember
                every { postRepository.save(any<Post>()) } returns post
                every { saveGroupPort.update(any()) } returns studyGroupWithCurrentRecentPost

                // When
                val result = postService.updatePost(memberInfo, postObjectId.toHexString(), postCommand)

                // Then
                result shouldNotBe null
                verify { postRepository.save(any<Post>()) }
                verify { saveGroupPort.update(any()) }
            }
        }
    }

    Given("게시글을 삭제할 때") {

        When("정상적인 요청으로 게시글을 삭제하면") {

            Then("게시글이 성공적으로 삭제되어야 한다") {
                // Given
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember
                every { postRepository.deleteById(postObjectId.toHexString()) } returns true

                // When
                postService.removePost(
                    memberInfo,
                    groupObjectId.toHexString(),
                    boardObjectId.toHexString(),
                    postObjectId.toHexString()
                )

                // Then
                verify { postRepository.deleteById(postObjectId.toHexString()) }
            }
        }

        When("존재하지 않는 게시글을 삭제하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { postRepository.findById(postObjectId.toHexString()) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.removePost(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        postObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 게시글을 찾을 수 없습니다."
            }
        }

        When("그룹의 멤버가 아닌 사용자가 게시글을 삭제하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.removePost(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        postObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 그룹의 멤버가 아닙니다."
            }
        }
    }

    Given("게시판의 게시글 목록을 조회할 때") {

        When("정상적인 요청으로 게시글 목록을 조회하면") {

            Then("게시글 목록이 성공적으로 반환되어야 한다") {
                // Given
                val posts = listOf(post)
                every { boardRepository.findById(boardObjectId) } returns board
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember
                every { postRepository.findByBoardId(boardObjectId.toHexString()) } returns posts

                // When
                val result = postService.getBoardPosts(
                    memberInfo,
                    groupObjectId.toHexString(),
                    boardObjectId.toHexString()
                )

                // Then
                result shouldNotBe null
                result.size shouldBe 1
                result[0].title shouldBe "테스트 제목"
            }
        }

        When("존재하지 않는 게시판의 게시글 목록을 조회하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.getBoardPosts(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 게시판을 찾을 수 없습니다."
            }
        }

        When("그룹의 멤버가 아닌 사용자가 게시글 목록을 조회하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { boardRepository.findById(boardObjectId) } returns board
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.getBoardPosts(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 그룹의 멤버가 아닙니다."
            }
        }

        When("조회 권한이 없는 사용자가 게시글 목록을 조회하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                val noReadPermissionBoard = createBoardWithoutReadPermission()
                every { boardRepository.findById(boardObjectId) } returns noReadPermissionBoard
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.getBoardPosts(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 게시판에 글 조회 권한이 없습니다."
            }
        }
    }

    Given("특정 게시글을 조회할 때") {

        When("정상적인 요청으로 게시글을 조회하면") {

            Then("게시글이 성공적으로 반환되어야 한다") {
                // Given
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { boardRepository.findById(boardObjectId) } returns board
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember

                // When
                val result = postService.getPost(
                    memberInfo,
                    groupObjectId.toHexString(),
                    boardObjectId.toHexString(),
                    postObjectId.toHexString()
                )

                // Then
                result shouldNotBe null
                result.title shouldBe "테스트 제목"
                result.content shouldBe "테스트 내용"
            }
        }

        When("존재하지 않는 게시글을 조회하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { postRepository.findById(postObjectId.toHexString()) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.getPost(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        postObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 게시글을 찾을 수 없습니다."
            }
        }

        When("게시글은 존재하지만 해당 게시판이 존재하지 않으면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { boardRepository.findById(boardObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.getPost(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        postObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 게시판을 찾을 수 없습니다."
            }
        }

        When("그룹의 멤버가 아닌 사용자가 게시글을 조회하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { boardRepository.findById(boardObjectId) } returns board
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns null

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.getPost(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        postObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 그룹의 멤버가 아닙니다."
            }
        }

        When("조회 권한이 없는 사용자가 게시글을 조회하려고 하면") {

            Then("NotFoundException이 발생해야 한다") {
                // Given
                val noReadPermissionBoard = createBoardWithoutReadPermission()
                every { postRepository.findById(postObjectId.toHexString()) } returns post
                every { boardRepository.findById(boardObjectId) } returns noReadPermissionBoard
                every { groupMemberRepository.findByGroupIdAndMemberId(groupObjectId, memberObjectId) } returns groupMember

                // When & Then
                val exception = shouldThrow<NotFoundException> {
                    postService.getPost(
                        memberInfo,
                        groupObjectId.toHexString(),
                        boardObjectId.toHexString(),
                        postObjectId.toHexString()
                    )
                }
                exception.message shouldBe "해당 게시판에 글 조회 권한이 없습니다."
            }
        }
    }
})
