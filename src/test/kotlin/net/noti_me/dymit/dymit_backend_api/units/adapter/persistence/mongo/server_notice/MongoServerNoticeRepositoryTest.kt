package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.server_notice

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.server_notice.MongoServerNoticeRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.server_notice.ServerNotice
import net.noti_me.dymit.dymit_backend_api.supports.createMemberEntity
import net.noti_me.dymit.dymit_backend_api.units.domain.server_notice.ServerNoticeTest.Companion.createServerNotice
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import kotlin.test.Test

@DataMongoTest
@Import(MongoConfig::class)
class MongoServerNoticeRepositoryTest(
    private val mongoTemplate: MongoTemplate
): AnnotationSpec() {
    private val repository = MongoServerNoticeRepository(mongoTemplate)

    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    private var notice: ServerNotice = createServerNotice()

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection("server_notices")
        notice = mongoTemplate.save(notice)
    }

    @Test
    fun `엔티티 저장 테스트`() {
        val newEntity = createServerNotice()
        val savedEntity = repository.save(newEntity)

        savedEntity.id shouldNotBe null
        savedEntity.title shouldBe newEntity.title
        savedEntity.content shouldBe newEntity.content
        savedEntity.writer.id shouldBe newEntity.writer.id
    }

    @Test
    fun `엔티티 수정 테스트`() {
        val writer = createMemberEntity(
            id = notice.writer.id,
            roles = listOf(MemberRole.ROLE_ADMIN)
        )
        val newTitle = "수정된 제목"
        val newContent = "수정된 내용"

        notice.updateContent(writer, newContent)
        notice.updateTitle(writer, newTitle)
        val updatedEntity = repository.save(notice)

        updatedEntity.id shouldBe notice.id
        updatedEntity.title shouldBe newTitle
        updatedEntity.content shouldBe newContent
    }

    @Test
    fun `엔티티 삭제 테스트`() {
        repository.delete(notice)

        val foundEntity = repository.findById(notice.id!!)
        foundEntity shouldBe null
    }

    @Test
    fun `엔티티 조회 테스트`() {
        val foundEntity = repository.findById(notice.id!!)
        foundEntity shouldNotBe null
        foundEntity!!.id shouldBe notice.id
    }

    @Test
    fun `다건 조회`() {
        val notices = repository.findAllByCursorIdOrderByIdDesc(null, 20)
        notices.size shouldBe 1
        notices[0].id shouldBe notice.id
    }
}
