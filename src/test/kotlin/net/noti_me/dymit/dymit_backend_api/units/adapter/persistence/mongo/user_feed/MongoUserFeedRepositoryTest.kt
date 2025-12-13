package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.user_feed

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.assertions.throwables.shouldThrow
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.user_feed.MongoUserFeedRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@DataMongoTest
@Import(MongoConfig::class)
class MongoUserFeedRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private val mongoUserFeedRepository = MongoUserFeedRepository(mongoTemplate)
    private val testMemberId = ObjectId()
    private val anotherMemberId = ObjectId()

    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection(UserFeed::class.java)
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(UserFeed::class.java)
    }

    @Test
    fun `save - 사용자 피드를 성공적으로 저장한다`() {
        // Given
        val userFeed = createUserFeed(testMemberId)

        // When
        val savedUserFeed = mongoUserFeedRepository.save(userFeed)

        // Then
        savedUserFeed.shouldNotBeNull()
        savedUserFeed.id.shouldNotBeNull()
        savedUserFeed.memberId shouldBe testMemberId
        savedUserFeed.isRead shouldBe false
    }

    @Test
    fun `save - 기존 사용자 피드를 업데이트한다`() {
        // Given
        val userFeed = createUserFeed(testMemberId)
        val savedUserFeed = mongoTemplate.save(userFeed)
        savedUserFeed.markAsRead()

        // When
        val updatedUserFeed = mongoUserFeedRepository.save(savedUserFeed)

        // Then
        updatedUserFeed.shouldNotBeNull()
        updatedUserFeed.id shouldBe savedUserFeed.id
        updatedUserFeed.isRead shouldBe true
    }

    @Test
    fun `findById - 존재하는 ID로 사용자 피드를 조회한다`() {
        // Given
        val userFeed = createUserFeed(testMemberId)
        val savedUserFeed = mongoTemplate.save(userFeed)

        // When
        val foundUserFeed = mongoUserFeedRepository.findById(savedUserFeed.identifier)

        // Then
        foundUserFeed.shouldNotBeNull()
        foundUserFeed.id shouldBe savedUserFeed.id
        foundUserFeed.memberId shouldBe testMemberId
    }

    @Test
    fun `findById - 존재하지 않는 ID로 조회시 null을 반환한다`() {
        // Given
        val nonExistentId = ObjectId().toHexString()

        // When
        val foundUserFeed = mongoUserFeedRepository.findById(nonExistentId)

        // Then
        foundUserFeed.shouldBeNull()
    }

    @Test
    fun `findById - 잘못된 형식의 ID로 조회시 예외가 발생한다`() {
        // Given
        val invalidId = "invalid-id"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            mongoUserFeedRepository.findById(invalidId)
        }
    }

    @Test
    fun `findByMemberId - cursor 없이 특정 멤버의 피드를 조회한다`() {
        // Given
        val userFeeds = listOf(
            createUserFeed(testMemberId, "메시지 1"),
            createUserFeed(testMemberId, "메시지 2"),
            createUserFeed(testMemberId, "메시지 3"),
            createUserFeed(anotherMemberId, "다른 멤버 메시지")
        )
        userFeeds.forEach { mongoTemplate.save(it) }

        // When
        val foundFeeds = mongoUserFeedRepository.findByMemberIdOrderByCreatedAtDesc(
            testMemberId.toHexString(),
            null,
            10L
        )

        // Then
        foundFeeds shouldHaveSize 3
        foundFeeds.forEach { it.memberId shouldBe testMemberId }
    }

    @Test
    fun `findByMemberId - cursor와 함께 특정 멤버의 피드를 조회한다`() {
        // Given
        val userFeeds = mutableListOf<UserFeed>()
        repeat(5) { i ->
            val userFeed = createUserFeed(testMemberId, "메시지 ${i + 1}")
            userFeeds.add(mongoTemplate.save(userFeed))
        }

        // 최신 순으로 정렬된 피드 중 중간 피드를 cursor로 사용
        val cursorFeed = userFeeds[2]

        // When
        val foundFeeds = mongoUserFeedRepository.findByMemberIdOrderByCreatedAtDesc(
            testMemberId.toHexString(),
            cursorFeed.identifier,
            10L
        )

        // Then
        foundFeeds.shouldNotBeNull()
        foundFeeds.forEach {
            it.memberId shouldBe testMemberId
            (it.identifier <= cursorFeed.identifier) shouldBe true
        }
    }

    @Test
    fun `findByMemberId - size 제한으로 피드를 조회한다`() {
        // Given
        repeat(10) { i ->
            val userFeed = createUserFeed(testMemberId, "메시지 ${i + 1}")
            mongoTemplate.save(userFeed)
        }

        // When
        val foundFeeds = mongoUserFeedRepository.findByMemberIdOrderByCreatedAtDesc(
            testMemberId.toHexString(),
            null,
            5L
        )

        // Then
        foundFeeds shouldHaveSize 5
        foundFeeds.forEach { it.memberId shouldBe testMemberId }
    }

    @Test
    fun `findByMemberId - 빈 cursor로 조회한다`() {
        // Given
        val userFeed = createUserFeed(testMemberId)
        mongoTemplate.save(userFeed)

        // When
        val foundFeeds = mongoUserFeedRepository.findByMemberIdOrderByCreatedAtDesc(
            testMemberId.toHexString(),
            "",
            10L
        )

        // Then
        foundFeeds shouldHaveSize 1
        foundFeeds[0].memberId shouldBe testMemberId
    }

    @Test
    fun `findByMemberId - 존재하지 않는 멤버 ID로 조회시 빈 리스트를 반환한다`() {
        // Given
        val userFeed = createUserFeed(testMemberId)
        mongoTemplate.save(userFeed)
        val nonExistentMemberId = ObjectId().toHexString()

        // When
        val foundFeeds = mongoUserFeedRepository.findByMemberIdOrderByCreatedAtDesc(
            nonExistentMemberId,
            null,
            10L
        )

        // Then
        foundFeeds shouldHaveSize 0
    }

    @Test
    fun `findByMemberId - 잘못된 형식의 멤버 ID로 조회시 예외가 발생한다`() {
        // Given
        val invalidMemberId = "invalid-member-id"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            mongoUserFeedRepository.findByMemberIdOrderByCreatedAtDesc(invalidMemberId, null, 10L)
        }
    }

    @Test
    fun `findByMemberId - 잘못된 형식의 cursor로 조회시 예외가 발생한다`() {
        // Given
        val userFeed = createUserFeed(testMemberId)
        mongoTemplate.save(userFeed)
        val invalidCursor = "invalid-cursor"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            mongoUserFeedRepository.findByMemberIdOrderByCreatedAtDesc(
                testMemberId.toHexString(),
                invalidCursor,
                10L
            )
        }
    }

    @Test
    fun `deleteById - 존재하는 ID로 사용자 피드를 삭제한다`() {
        // Given
        val userFeed = createUserFeed(testMemberId)
        val savedUserFeed = mongoTemplate.save(userFeed)

        // When
        val isDeleted = mongoUserFeedRepository.deleteById(savedUserFeed.identifier)

        // Then
        isDeleted shouldBe true
        val foundUserFeed = mongoTemplate.findById(savedUserFeed.identifier, UserFeed::class.java)
        foundUserFeed.shouldBeNull()
    }

    @Test
    fun `deleteById - 존재하지 않는 ID로 삭제시 false를 반환한다`() {
        // Given
        val nonExistentId = ObjectId().toHexString()

        // When
        val isDeleted = mongoUserFeedRepository.deleteById(nonExistentId)

        // Then
        isDeleted shouldBe false
    }

    @Test
    fun `deleteById - 잘못된 형식의 ID로 삭제시 예외가 발생한다`() {
        // Given
        val invalidId = "invalid-id"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            mongoUserFeedRepository.deleteById(invalidId)
        }
    }

    private fun createUserFeed(
        memberId: ObjectId,
        message: String = "테스트 메시지"
    ): UserFeed {
        return UserFeed(
            memberId = memberId,
            messages = listOf(
                FeedMessage(
                    text = message
                )
            ),
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.MEMBER,
                    resourceId = memberId.toHexString()
                )
            ),
            iconType = IconType.CHECK,
            eventName = "Sample Event"
        )
    }
}
