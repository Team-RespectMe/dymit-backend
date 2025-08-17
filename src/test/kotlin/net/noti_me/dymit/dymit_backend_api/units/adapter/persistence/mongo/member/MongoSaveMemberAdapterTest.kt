package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.member

import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.member.MongoSaveMemberAdapter
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.springframework.data.mongodb.core.MongoTemplate
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

// @TestPropertySource(properties = ["de.flapdoodle.mongodb.embedded.version=5.3.0"])
// @TestPropertySource(locations = ["classpath:application.yaml"])
@DataMongoTest
class MongoSaveMemberAdapterTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    override fun extensions(): List<Extension> =  listOf(
        SpringExtension
    )

    private val saveAdapter = MongoSaveMemberAdapter(mongoTemplate)

    private var existingMember: Member? = null

    @BeforeEach
    fun setUp() {
        existingMember = mongoTemplate.save(Member(
            nickname = "test-nickname",
            oidcIdentities = mutableSetOf(OidcIdentity(provider = "test-provider", subject = "test-subject"))
        ))
    }

    @Test
    fun `신규 멤버 저장 테스트`() {
        val newMember = Member(
            nickname = "new-nickname",
            oidcIdentities = mutableSetOf(OidcIdentity(provider = "new-provider", subject = "new-subject"))
        )

        val savedMember = saveAdapter.persist(newMember)

        savedMember.id shouldNotBe null
        savedMember.nickname shouldBe "new-nickname"
        savedMember.oidcIdentities.size shouldBe 1
        savedMember.oidcIdentities.first().provider shouldBe "new-provider"
        savedMember.oidcIdentities.first().subject shouldBe "new-subject"
    }

    @Test
    fun `기존 멤버 업데이트 테스트`() {
        existingMember?.let { member ->
            member.changeNickname("updated-nickname")
            val updatedMember = saveAdapter.update(member)

            updatedMember.nickname shouldBe "updated-nickname"
        }
    }

    @Test
    fun `멤버 삭제 테스트`() {
        existingMember?.let { member ->
            member.markAsDeleted()
            val isDeleted = saveAdapter.delete(member)
            isDeleted shouldBe true
            val deletedMember = mongoTemplate.findById(member.id!!, Member::class.java)
            deletedMember?.isDeleted shouldBe true
        }
    }
}
