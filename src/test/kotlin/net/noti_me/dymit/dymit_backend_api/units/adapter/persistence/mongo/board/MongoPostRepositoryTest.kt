package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.board

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board.MongoPostRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.board.Post
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@DataMongoTest
@Import(MongoConfig::class)
class MongoPostRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private var existingPost: Post? = null
    private val testGroupId = ObjectId()
    private val testBoardId = ObjectId()
    private val mongoPostRepository = MongoPostRepository(mongoTemplate)

    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection(Post::class.java)
        existingPost = createPost()
        existingPost = mongoTemplate.save(existingPost!!)
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(Post::class.java)
    }

    @Test
    fun `게시글 저장 테스트`() {
        // Given
        val newPost = createPost()

        // When
        val savedPost = mongoPostRepository.save(newPost)

        // Then
        savedPost.shouldNotBeNull()
        savedPost.id shouldBe newPost.id
        savedPost.title shouldBe newPost.title
        savedPost.content shouldBe newPost.content
        savedPost.groupId shouldBe newPost.groupId
        savedPost.writer.id shouldBe newPost.writer.id
    }

    @Test
    fun `게시글 ID로 조회 테스트`() {
        // Given
        val postId = existingPost?.id?.toHexString()
            ?: throw IllegalStateException("Existing post not found")

        // When
        val foundPost = mongoPostRepository.findById(postId)

        // Then
        foundPost.shouldNotBeNull()
        foundPost.id shouldBe existingPost!!.id
        foundPost.title shouldBe existingPost!!.title
        foundPost.content shouldBe existingPost!!.content
        foundPost.groupId shouldBe existingPost!!.groupId
    }

    @Test
    fun `존재하지 않는 ID로 조회 테스트`() {
        // Given
        val nonExistentId = ObjectId().toHexString()

        // When
        val foundPost = mongoPostRepository.findById(nonExistentId)

        // Then
        foundPost.shouldBeNull()
    }

    @Test
    fun `잘못된 형식의 ID로 조회 테스트`() {
        // Given
        val invalidId = "invalid-id"

        // When
        val foundPost = mongoPostRepository.findById(invalidId)

        // Then
        foundPost.shouldBeNull()
    }

    @Test
    fun `그룹 ID로 게시글 목록 조회 테스트`() {
        // Given
        val anotherPost = createPost(groupId = testGroupId)
        mongoTemplate.save(anotherPost)

        // When
        val posts = mongoPostRepository.findByGroupId(testGroupId.toHexString())

        // Then
        posts shouldHaveSize 2
        posts[0].groupId shouldBe testGroupId
    }

    @Test
    fun `존재하지 않는 그룹 ID로 조회 테스트`() {
        // Given
        val nonExistentGroupId = ObjectId().toHexString()

        // When
        val posts = mongoPostRepository.findByGroupId(nonExistentGroupId)

        // Then
        posts shouldHaveSize 0
    }

    @Test
    fun `잘못된 형식의 그룹 ID로 조회 테스트`() {
        // Given
        val invalidGroupId = "invalid-group-id"

        // When
        val posts = mongoPostRepository.findByGroupId(invalidGroupId)

        // Then
        posts shouldHaveSize 0
    }

    @Test
    fun `게시글 삭제 테스트`() {
        // Given
        val postId = existingPost?.id?.toHexString()
            ?: throw IllegalStateException("Existing post not found")

        // When
        val deleteResult = mongoPostRepository.deleteById(postId)

        // Then
        deleteResult shouldBe true

        // Verify deletion
        val deletedPost = mongoPostRepository.findById(postId)
        deletedPost.shouldBeNull()
    }

    @Test
    fun `존재하지 않는 게시글 삭제 테스트`() {
        // Given
        val nonExistentId = ObjectId().toHexString()

        // When
        val deleteResult = mongoPostRepository.deleteById(nonExistentId)

        // Then
        deleteResult shouldBe false
    }

    @Test
    fun `잘못된 형식의 ID로 삭제 테스트`() {
        // Given
        val invalidId = "invalid-id"

        // When
        val deleteResult = mongoPostRepository.deleteById(invalidId)

        // Then
        deleteResult shouldBe false
    }

    private fun createPost(
        id: ObjectId = ObjectId(),
        groupId: ObjectId = testGroupId,
        boardId: ObjectId = testBoardId,
        title: String = "테스트 게시글",
        content: String = "테스트 게시글 내용"
    ): Post {
        val writer = Writer(
            id = ObjectId(),
            nickname = "테스트작성자",
            image = ProfileImageVo(
                type = "profile",
                url = "http://example.com/image.jpg"
            )
        )

        return Post(
            id = id,
            groupId = groupId,
            boardId = boardId,
            writer = writer,
            title = title,
            content = content
        )
    }
}
