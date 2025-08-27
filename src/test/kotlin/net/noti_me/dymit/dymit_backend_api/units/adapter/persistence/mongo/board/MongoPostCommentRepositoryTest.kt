package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.board

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.board.MongoPostCommentRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.board.PostComment
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@DataMongoTest
@Import(MongoConfig::class)
class MongoPostCommentRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private var existingComment: PostComment? = null
    private val testPostId = ObjectId()
    private val mongoPostCommentRepository = MongoPostCommentRepository(mongoTemplate)

    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection(PostComment::class.java)
        existingComment = createComment(postId = testPostId)
        existingComment = mongoTemplate.save(existingComment!!)
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(PostComment::class.java)
    }

    @Test
    fun `댓글 저장 테스트`() {
        // Given
        val newComment = createComment()

        // When
        val savedComment = mongoPostCommentRepository.save(newComment)

        // Then
        savedComment.shouldNotBeNull()
        savedComment.id shouldBe newComment.id
        savedComment.content shouldBe newComment.content
        savedComment.postId shouldBe newComment.postId
        savedComment.writer.id shouldBe newComment.writer.id
    }

    @Test
    fun `댓글 ID로 조회 테스트`() {
        // Given
        val commentId = existingComment?.id?.toHexString()
            ?: throw IllegalStateException("Existing comment not found")

        // When
        val foundComment = mongoPostCommentRepository.findById(commentId)

        // Then
        foundComment.shouldNotBeNull()
        foundComment.id shouldBe existingComment!!.id
        foundComment.content shouldBe existingComment!!.content
        foundComment.postId shouldBe existingComment!!.postId
        foundComment.writer.id shouldBe existingComment!!.writer.id
    }

    @Test
    fun `존재하지 않는 ID로 조회 테스트`() {
        // Given
        val nonExistentId = ObjectId().toHexString()

        // When
        val foundComment = mongoPostCommentRepository.findById(nonExistentId)

        // Then
        foundComment.shouldBeNull()
    }

    @Test
    fun `게시물 ID로 댓글 목록 조회 테스트`() {
        // Given
        val anotherComment = createComment(postId = testPostId)
        mongoTemplate.save(anotherComment)
        val postIdString = testPostId.toHexString()

        // When
        val comments = mongoPostCommentRepository.findByPostId(postIdString)

        // Then
        comments.shouldNotBeNull()
        comments shouldHaveSize 2
        comments[0].postId shouldBe testPostId
    }

    @Test
    fun `존재하지 않는 게시물 ID로 댓글 목록 조회 테스트`() {
        // Given
        val nonExistentPostId = ObjectId().toHexString()

        // When
        val comments = mongoPostCommentRepository.findByPostId(nonExistentPostId)

        // Then
        comments shouldHaveSize 0
    }

    @Test
    fun `ID로 댓글 삭제 테스트`() {
        // Given
        val commentId = existingComment?.id?.toHexString()
            ?: throw IllegalStateException("Existing comment not found")

        // When
        val deleteResult = mongoPostCommentRepository.deleteById(commentId)

        // Then
        deleteResult shouldBe true

        // Verify deletion
        val foundComment = mongoTemplate.findById(existingComment!!.id, PostComment::class.java)
        foundComment.shouldBeNull()
    }

    @Test
    fun `존재하지 않는 ID로 삭제 테스트`() {
        // Given
        val nonExistentId = ObjectId().toHexString()

        // When
        val deleteResult = mongoPostCommentRepository.deleteById(nonExistentId)

        // Then
        deleteResult shouldBe false
    }

    @Test
    fun `댓글 객체로 삭제 테스트`() {
        // Given
        val comment = existingComment ?: throw IllegalStateException("Existing comment not found")

        // When
        val deleteResult = mongoPostCommentRepository.delete(comment)

        // Then
        deleteResult shouldBe true

        // Verify deletion
        val foundComment = mongoTemplate.findById(comment.id, PostComment::class.java)
        foundComment.shouldBeNull()
    }

    @Test
    fun `존재하지 않는 댓글 객체로 삭제 테스트`() {
        // Given
        val nonExistentComment = createComment(id = ObjectId())

        // When
        val deleteResult = mongoPostCommentRepository.delete(nonExistentComment)

        // Then
        deleteResult shouldBe false
    }

    private fun createComment(
        id: ObjectId = ObjectId(),
        postId: ObjectId = testPostId,
        content: String = "테스트 댓글 내용입니다."
    ): PostComment {
        val writer = Writer(
            id = ObjectId(),
            nickname = "테스트 작성자",
            image = ProfileImageVo(
                type = "URL",
                url = "https://example.com/profile.jpg"
            )
        )

        return PostComment(
            id = id,
            postId = postId,
            writer = writer,
            content = content
        )
    }
}
