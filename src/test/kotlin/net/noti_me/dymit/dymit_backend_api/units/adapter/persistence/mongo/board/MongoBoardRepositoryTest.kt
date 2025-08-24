package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.board

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board.MongoBoardRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.board.Board
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardAction
import net.noti_me.dymit.dymit_backend_api.domain.board.BoardPermission
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupMemberRole
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@DataMongoTest
@Import(MongoConfig::class)
class MongoBoardRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private var existingBoard: Board? = null
    private val testGroupId = ObjectId()
    private val mongoBoardRepository = MongoBoardRepository(mongoTemplate)

    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection(Board::class.java)
        existingBoard = createBoard()
        existingBoard = mongoTemplate.save(existingBoard!!)
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(Board::class.java)
    }

    @Test
    fun `게시판 저장 테스트`() {
        // Given
        val newBoard = createBoard()

        // When
        val savedBoard = mongoBoardRepository.save(newBoard)

        // Then
        savedBoard.shouldNotBeNull()
        savedBoard.id shouldBe newBoard.id
        savedBoard.name shouldBe newBoard.name
        savedBoard.groupId shouldBe newBoard.groupId
        savedBoard.permissions shouldHaveSize newBoard.permissions.size
    }

    @Test
    fun `게시판 ID로 조회 테스트`() {
        // Given
        val boardId = existingBoard?.id
            ?: throw IllegalStateException("Existing board not found")

        // When
        val foundBoard = mongoBoardRepository.findById(boardId)

        // Then
        foundBoard.shouldNotBeNull()
        foundBoard.id shouldBe existingBoard!!.id
        foundBoard.name shouldBe existingBoard!!.name
        foundBoard.groupId shouldBe existingBoard!!.groupId
    }

    @Test
    fun `존재하지 않는 ID로 조회 테스트`() {
        // Given
        val nonExistentId = ObjectId()

        // When
        val foundBoard = mongoBoardRepository.findById(nonExistentId)

        // Then
        foundBoard.shouldBeNull()
    }

    @Test
    fun `그룹 ID로 게시판 목록 조회 테스트`() {
        // Given
        val anotherBoard = createBoard(groupId = testGroupId)
        mongoTemplate.save(anotherBoard)

        // When
        val boards = mongoBoardRepository.findByGroupId(testGroupId)

        // Then
        boards shouldHaveSize 2
        boards[0].groupId shouldBe testGroupId
    }

    @Test
    fun `존재하지 않는 그룹 ID로 조회 테스트`() {
        // Given
        val nonExistentGroupId = ObjectId()

        // When
        val boards = mongoBoardRepository.findByGroupId(nonExistentGroupId)

        // Then
        boards shouldHaveSize 0
    }

    @Test
    fun `여러 게시판이 있는 그룹 조회 테스트`() {
        // Given
        val board1 = createBoard(groupId = testGroupId, name = "게시판1")
        val board2 = createBoard(groupId = testGroupId, name = "게시판2")
        mongoTemplate.save(board1)
        mongoTemplate.save(board2)

        // When
        val boards = mongoBoardRepository.findByGroupId(testGroupId)

        // Then
        boards shouldHaveSize 3
        boards.all { it.groupId == testGroupId } shouldBe true
    }

    @Test
    fun `게시판 ID로 삭제 테스트`() {
        // Given
        val boardId = existingBoard?.id
            ?: throw IllegalStateException("Existing board not found")

        // When
        val deleteResult = mongoBoardRepository.deleteById(boardId)

        // Then
        deleteResult shouldBe true

        // Verify deletion
        val deletedBoard = mongoBoardRepository.findById(boardId)
        deletedBoard.shouldBeNull()
    }

    @Test
    fun `존재하지 않는 게시판 ID로 삭제 테스트`() {
        // Given
        val nonExistentId = ObjectId()

        // When
        val deleteResult = mongoBoardRepository.deleteById(nonExistentId)

        // Then
        deleteResult shouldBe false
    }

    @Test
    fun `게시판 객체로 삭제 테스트`() {
        // Given
        val boardToDelete = existingBoard!!

        // When
        val deleteResult = mongoBoardRepository.delete(boardToDelete)

        // Then
        deleteResult shouldBe true

        // Verify deletion
        val deletedBoard = mongoBoardRepository.findById(boardToDelete.id)
        deletedBoard.shouldBeNull()
    }

    @Test
    fun `존재하지 않는 게시판 객체로 삭제 테스트`() {
        // Given
        val nonExistentBoard = createBoard(id = ObjectId())

        // When
        val deleteResult = mongoBoardRepository.delete(nonExistentBoard)

        // Then
        deleteResult shouldBe false
    }

    @Test
    fun `MongoDB 연결 오류 시 findById 예외 처리 테스트`() {
        // Given
        val invalidId = ObjectId()

        // MongoDB에서 컬렉션을 완전히 제거하여 예외 상황 시뮬레이션
        mongoTemplate.dropCollection(Board::class.java)

        // When
        val result = mongoBoardRepository.findById(invalidId)

        // Then - 예외가 발생해도 null 반환
        result.shouldBeNull()
    }

    @Test
    fun `MongoDB 연결 오류 시 findByGroupId 예외 처리 테스트`() {
        // Given
        val invalidGroupId = ObjectId()

        // MongoDB에서 컬렉션을 완전히 제거하여 예외 상황 시뮬레이션
        mongoTemplate.dropCollection(Board::class.java)

        // When
        val result = mongoBoardRepository.findByGroupId(invalidGroupId)

        // Then - 예외가 발생해도 빈 리스트 반환
        result shouldHaveSize 0
    }

    @Test
    fun `MongoDB 연결 오류 시 deleteById 예외 처리 테스트`() {
        // Given
        val invalidId = ObjectId()

        // MongoDB에서 컬렉션을 완전히 제거하여 예외 상황 시뮬레이션
        mongoTemplate.dropCollection(Board::class.java)

        // When
        val result = mongoBoardRepository.deleteById(invalidId)

        // Then - 예외가 발생해도 false 반환
        result shouldBe false
    }

    @Test
    fun `MongoDB 연결 오류 시 delete 예외 처리 테스트`() {
        // Given
        val board = createBoard()

        // MongoDB에서 컬렉션을 완전히 제거하여 예외 상황 시뮬레이션
        mongoTemplate.dropCollection(Board::class.java)

        // When
        val result = mongoBoardRepository.delete(board)

        // Then - 예외가 발생해도 false 반환
        result shouldBe false
    }

    private fun createBoard(
        id: ObjectId = ObjectId(),
        groupId: ObjectId = testGroupId,
        name: String = "테스트 게시판"
    ): Board {
        val permissions = mutableSetOf(
            BoardPermission(
                role = GroupMemberRole.OWNER,
                actions = mutableListOf(BoardAction.MANAGE_BOARD, BoardAction.WRITE_POST)
            ),
            BoardPermission(
                role = GroupMemberRole.MEMBER,
                actions = mutableListOf(BoardAction.READ_POST)
            )
        )

        return Board(
            id = id,
            groupId = groupId,
            name = name,
            permissions = permissions
        )
    }
}
