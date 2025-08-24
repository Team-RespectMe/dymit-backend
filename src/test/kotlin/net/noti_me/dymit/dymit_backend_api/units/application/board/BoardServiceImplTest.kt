package net.noti_me.dymit.dymit_backend_api.units.application.board

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.board.dto.BoardCommand
import net.noti_me.dymit.dymit_backend_api.application.board.impl.BoardServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.board.BoardRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import java.time.LocalDateTime

class BoardServiceImplTest : AnnotationSpec() {

    private val boardRepository = mockk<BoardRepository>()
    private val studyGroupMemberRepository = mockk<StudyGroupMemberRepository>()
    private val boardService = BoardServiceImpl(boardRepository, studyGroupMemberRepository)

    private val testMemberId = ObjectId()
    private val testGroupId = ObjectId()
    private val testBoardId = ObjectId()
    private val memberInfo = MemberInfo(
        memberId = testMemberId.toHexString(),
        nickname = "테스트멤버",
        roles = listOf(MemberRole.ROLE_MEMBER)
    )

    private val testProfileImage = MemberProfileImageVo(
        type = "presets",
        filePath = "/images/profile/test.jpg",
        url = "https://example.com/test.jpg",
        fileSize = 1024L,
        width = 200,
        height = 200
    )

    private val testGroupMember = StudyGroupMember(
        id = ObjectId(),
        groupId = testGroupId,
        memberId = testMemberId,
        nickname = "테스트 멤버",
        profileImage = testProfileImage,
        role = GroupMemberRole.OWNER
    )

    private val testBoard = Board(
        id = testBoardId,
        groupId = testGroupId,
        name = "테스트 게시판",
        permissions = mutableSetOf(
            BoardPermission(GroupMemberRole.OWNER, mutableListOf(BoardAction.MANAGE_BOARD))
        )
    )

    private val testBoardCommand = BoardCommand(
        name = "테스트 게시판",
        permissions = listOf(
            BoardPermission(GroupMemberRole.OWNER, mutableListOf(BoardAction.MANAGE_BOARD))
        )
    )

    @Test
    fun `createBoard - 성공적으로 게시판을 생성한다`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.save(any()) } returns testBoard

        // When
        val result = boardService.createBoard(memberInfo, testGroupId.toHexString(), testBoardCommand)
        println(result.name)
        // Then
        result shouldNotBe null
        result.name shouldBe testBoardCommand.name
        result.groupId shouldBe testGroupId.toHexString()

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.save(any()) }
    }

    @Test
    fun `createBoard - 그룹 멤버가 아닌 경우 ForbiddenException 발생`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns null

        // When & Then
        shouldThrow<ForbiddenException> {
            boardService.createBoard(memberInfo, testGroupId.toHexString(), testBoardCommand)
        }.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify(exactly = 0) { boardRepository.save(any()) }
    }

    @Test
    fun `createBoard - 게시판 저장 실패시 RuntimeException 발생`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.save(any()) } returns null

        // When & Then
        shouldThrow<RuntimeException> {
            boardService.createBoard(memberInfo, testGroupId.toHexString(), testBoardCommand)
        }.message shouldBe "게시판 생성에 실패했습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.save(any()) }
    }

    @Test
    fun `updateBoard - 성공적으로 게시판을 업데이트한다`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.findById(testBoardId) } returns testBoard
        every { boardRepository.save(testBoard) } returns testBoard

        // When
        val result = boardService.updateBoard(
            memberInfo,
            testGroupId.toHexString(),
            testBoardId.toHexString(),
            testBoardCommand
        )

        // Then
        result shouldNotBe null
        result.name shouldBe testBoardCommand.name

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.findById(testBoardId) }
        verify { boardRepository.save(testBoard) }
    }

    @Test
    fun `updateBoard - 그룹 멤버가 아닌 경우 ForbiddenException 발생`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns null

        // When & Then
        shouldThrow<ForbiddenException> {
            boardService.updateBoard(
                memberInfo,
                testGroupId.toHexString(),
                testBoardId.toHexString(),
                testBoardCommand
            )
        }.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify(exactly = 0) { boardRepository.findById(any()) }
    }

    @Test
    fun `updateBoard - 게시판이 존재하지 않는 경우 NotFoundException 발생`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.findById(testBoardId) } returns null

        // When & Then
        shouldThrow<NotFoundException> {
            boardService.updateBoard(
                memberInfo,
                testGroupId.toHexString(),
                testBoardId.toHexString(),
                testBoardCommand
            )
        }.message shouldBe "요청한 리소스를 찾을 수 없습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.findById(testBoardId) }
    }

    @Test
    fun `updateBoard - 다른 그룹의 게시판인 경우 ForbiddenException 발생`() {
        // Given
        val otherGroupId = ObjectId()
        val otherGroupBoard = Board(
            id = testBoardId,
            groupId = otherGroupId,
            name = "다른 그룹 게시판",
            permissions = mutableSetOf()
        )

        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.findById(testBoardId) } returns otherGroupBoard

        // When & Then
        shouldThrow<ForbiddenException> {
            boardService.updateBoard(
                memberInfo,
                testGroupId.toHexString(),
                testBoardId.toHexString(),
                testBoardCommand
            )
        }.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.findById(testBoardId) }
    }

    @Test
    fun `removeBoard - 성공적으로 게시판을 삭제한다`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.findById(testBoardId) } returns testBoard
        every { boardRepository.delete(testBoard) } returns true

        // When
        boardService.removeBoard(memberInfo, testGroupId.toHexString(), testBoardId.toHexString())

        // Then
        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.findById(testBoardId) }
        verify { boardRepository.delete(testBoard) }
    }

    @Test
    fun `removeBoard - 그룹 멤버가 아닌 경우 ForbiddenException 발생`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns null

        // When & Then
        shouldThrow<ForbiddenException> {
            boardService.removeBoard(memberInfo, testGroupId.toHexString(), testBoardId.toHexString())
        }.message shouldBe "권한이 없어 요청을 처리할 수 없습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify(exactly = 0) { boardRepository.findById(any()) }
    }

    @Test
    fun `removeBoard - 게시판이 존재하지 않는 경우 NotFoundException 발생`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.findById(testBoardId) } returns null

        // When & Then
        shouldThrow<NotFoundException> {
            boardService.removeBoard(memberInfo, testGroupId.toHexString(), testBoardId.toHexString())
        }.message shouldBe "요청한 리소스를 찾을 수 없습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.findById(testBoardId) }
    }

    @Test
    fun `removeBoard - 삭제 실패시 RuntimeException 발생`() {
        // Given
        every { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) } returns testGroupMember
        every { boardRepository.findById(testBoardId) } returns testBoard
        every { boardRepository.delete(testBoard) } returns false

        // When & Then
        shouldThrow<RuntimeException> {
            boardService.removeBoard(memberInfo, testGroupId.toHexString(), testBoardId.toHexString())
        }.message shouldBe "게시판 삭제에 실패했습니다."

        verify { studyGroupMemberRepository.findByGroupIdAndMemberId(testGroupId, testMemberId) }
        verify { boardRepository.findById(testBoardId) }
        verify { boardRepository.delete(testBoard) }
    }

    @Test
    fun `getGroupBoards - 해당 그룹의 게시판 목록을 반환한다`() {
        // Given
        val board1 = Board(
            id = ObjectId(),
            groupId = testGroupId,
            name = "게시판1",
            permissions = mutableSetOf()
        )

        val board2 = Board(
            id = ObjectId(),
            groupId = testGroupId,
            name = "게시판2",
            permissions = mutableSetOf()
        )

        every { boardRepository.findByGroupId(testGroupId) } returns listOf(board1, board2)

        // When
        val result = boardService.getGroupBoards(testGroupId.toHexString())

        // Then
        result shouldHaveSize 2
        result[0].name shouldBe "게시판1"
        result[1].name shouldBe "게시판2"
        result.all { it.groupId == testGroupId.toHexString() } shouldBe true

        verify { boardRepository.findByGroupId(testGroupId) }
    }

    @Test
    fun `getGroupBoards - 게시판이 없는 경우 빈 목록을 반환한다`() {
        // Given
        every { boardRepository.findByGroupId(testGroupId) } returns emptyList()

        // When
        val result = boardService.getGroupBoards(testGroupId.toHexString())

        // Then
        result shouldHaveSize 0

        verify { boardRepository.findByGroupId(testGroupId) }
    }

    @AfterEach
    fun clear() {
        clearAllMocks()
    }
}
