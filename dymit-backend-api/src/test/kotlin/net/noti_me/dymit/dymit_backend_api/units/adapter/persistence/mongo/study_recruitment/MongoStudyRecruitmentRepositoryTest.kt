package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.study_recruitment

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_recruitment.MongoStudyRecruitmentRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.study_recruitment.StudyRecruitment
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import kotlin.test.Test

/**
 * MongoStudyRecruitmentRepository 구현체 테스트입니다.
 *
 * 커서 기반 조회와 삭제 여부 필터링이 올바르게 동작하는지 검증합니다.
 */
@DataMongoTest
@Import(MongoConfig::class)
class MongoStudyRecruitmentRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private val repository = MongoStudyRecruitmentRepository(mongoTemplate)

    override fun extensions(): List<Extension> {
        return listOf(SpringExtension)
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection("study_recruitments")
    }

    @Test
    fun `삭제되지 않은 모집글만 조회한다`() {
        // Given
        val active = mongoTemplate.save(createStudyRecruitment(externalId = "active", isDeleted = false))
        mongoTemplate.save(createStudyRecruitment(externalId = "deleted", isDeleted = true))

        // When
        val result = repository.findByCursorOrderByIdDesc(null, 10)

        // Then
        result shouldHaveSize 1
        result[0].id shouldBe active.id
        result[0].externalId shouldBe "active"
    }

    @Test
    fun `커서 기반으로 다음 페이지를 조회한다`() {
        // Given
        repeat(5) { index ->
            mongoTemplate.save(createStudyRecruitment(externalId = "recruitment-$index", isDeleted = false))
            Thread.sleep(10)
        }

        val firstPage = repository.findByCursorOrderByIdDesc(null, 3)
        val cursorId = firstPage.last().id!!

        // When
        val secondPage = repository.findByCursorOrderByIdDesc(cursorId, 3)

        // Then
        firstPage shouldHaveSize 3
        secondPage shouldHaveSize 2
        secondPage.forEach { item ->
            (item.id!!.toHexString() < cursorId.toHexString()) shouldBe true
        }
    }

    private fun createStudyRecruitment(
        externalId: String,
        isDeleted: Boolean
    ): StudyRecruitment {
        return StudyRecruitment(
            id = ObjectId.get(),
            externalId = externalId,
            type = "INFLEARN",
            title = "title-$externalId",
            content = "content-$externalId",
            url = "https://example.com/$externalId",
            writer = "writer-$externalId",
            isDeleted = isDeleted
        )
    }
}
