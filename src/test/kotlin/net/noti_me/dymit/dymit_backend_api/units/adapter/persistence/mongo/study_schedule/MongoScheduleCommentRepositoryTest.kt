package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.study_schedule

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_schedule.MongoScheduleCommentRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@DataMongoTest
@Import(MongoConfig::class)
class MongoScheduleCommentRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private var existingComment: ScheduleComment? = null
    private val testScheduleId = ObjectId()
    private val testMemberId = ObjectId()
    private val mongoScheduleCommentRepository = MongoScheduleCommentRepository(mongoTemplate)

    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection(ScheduleComment::class.java)
        existingComment = createScheduleComment(scheduleId = testScheduleId, memberId = testMemberId)
        existingComment = mongoTemplate.save(existingComment!!)
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(ScheduleComment::class.java)
    }

    @Test
    fun `스케줄 댓글 저장 테스트`() {
        // Given
        val newComment = createScheduleComment()

        // When
        val savedComment = mongoScheduleCommentRepository.save(newComment)

        // Then
        savedComment.shouldNotBeNull()
        savedComment.id shouldBe newComment.id
        savedComment.content shouldBe newComment.content
        savedComment.scheduleId shouldBe newComment.scheduleId
        savedComment.writer.id shouldBe newComment.writer.id
    }

    @Test
    fun `댓글 ID로 조회 테스트`() {
        // Given
        val commentId = existingComment?.id ?: throw IllegalStateException("Existing comment not found")

        // When
        val foundComment = mongoScheduleCommentRepository.findById(commentId)

        // Then
        foundComment.shouldNotBeNull()
        foundComment.id shouldBe existingComment!!.id
        foundComment.content shouldBe existingComment!!.content
        foundComment.scheduleId shouldBe existingComment!!.scheduleId
        foundComment.writer.id shouldBe existingComment!!.writer.id
    }

    @Test
    fun `존재하지 않는 ID로 조회 테스트`() {
        // Given
        val nonExistentId = ObjectId()

        // When
        val foundComment = mongoScheduleCommentRepository.findById(nonExistentId)

        // Then
        foundComment.shouldBeNull()
    }

    @Test
    fun `멤버 ID로 댓글 목록 조회 테스트`() {
        // Given
        val anotherComment = createScheduleComment(memberId = testMemberId)
        mongoTemplate.save(anotherComment)

        // When
        val comments = mongoScheduleCommentRepository.findByMemberId(testMemberId)

        // Then
        comments.shouldNotBeNull()
        comments shouldHaveSize 2
        comments.forEach { comment ->
            comment.writer.id shouldBe testMemberId
        }
    }

    @Test
    fun `존재하지 않는 멤버 ID로 댓글 목록 조회 테스트`() {
        // Given
        val nonExistentMemberId = ObjectId()

        // When
        val comments = mongoScheduleCommentRepository.findByMemberId(nonExistentMemberId)

        // Then
        comments shouldHaveSize 0
    }

    @Test
    fun `스케줄 ID로 댓글 목록 조회 테스트 - 커서 페이징`() {
        // Given
        val comment1 = createScheduleComment(scheduleId = testScheduleId)
        val comment2 = createScheduleComment(scheduleId = testScheduleId)
        val comment3 = createScheduleComment(scheduleId = testScheduleId)

        val savedComment1 = mongoTemplate.save(comment1)
        Thread.sleep(1) // ID 생성 시간 차이를 위한 대기
        val savedComment2 = mongoTemplate.save(comment2)
        Thread.sleep(1)
        val savedComment3 = mongoTemplate.save(comment3)

        // When - 최신 댓글부터 2개 조회 (DESC 정렬)
        val cursor = savedComment3.id
        val comments = mongoScheduleCommentRepository.findByScheduleId(testScheduleId, cursor, 2)

        // Then
        comments.shouldNotBeNull()
        comments shouldHaveSize 2
        comments.forEach { comment ->
            comment.scheduleId shouldBe testScheduleId
        }
    }

    @Test
    fun `존재하지 않는 스케줄 ID로 댓글 목록 조회 테스트`() {
        // Given
        val nonExistentScheduleId = ObjectId()
        val cursor = ObjectId()

        // When
        val comments = mongoScheduleCommentRepository.findByScheduleId(nonExistentScheduleId, cursor, 10)

        // Then
        comments shouldHaveSize 0
    }

    @Test
    fun `ID로 댓글 삭제 테스트`() {
        // Given
        val commentId = existingComment?.id ?: throw IllegalStateException("Existing comment not found")

        // When
        mongoScheduleCommentRepository.deleteById(commentId)

        // Then
        val foundComment = mongoTemplate.findById(commentId, ScheduleComment::class.java)
        foundComment.shouldBeNull()
    }

    @Test
    fun `존재하지 않는 ID로 삭제 테스트`() {
        // Given
        val nonExistentId = ObjectId()
        val beforeCount = mongoTemplate.findAll(ScheduleComment::class.java).size

        // When
        mongoScheduleCommentRepository.deleteById(nonExistentId)

        // Then
        val afterCount = mongoTemplate.findAll(ScheduleComment::class.java).size
        afterCount shouldBe beforeCount
    }

    private fun createScheduleComment(
        id: ObjectId = ObjectId(),
        scheduleId: ObjectId = testScheduleId,
        memberId: ObjectId = testMemberId,
        content: String = "테스트 스케줄 댓글 내용입니다."
    ): ScheduleComment {
        val writer = Writer(
            id = memberId,
            nickname = "테스트 작성자",
            image = ProfileImageVo(
                type = "URL",
                url = "https://example.com/profile.jpg"
            )
        )

        return ScheduleComment(
            id = id,
            scheduleId = scheduleId,
            writer = writer,
            content = content
        )
    }
}
