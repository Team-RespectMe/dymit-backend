package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.MongoLoadMemberAdapter
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@DataMongoTest
@Import(MongoConfig::class)
//@SpringBootTest
class MongoLoadMemberAdapterTest(
    private val mongoTemplate: MongoTemplate
): AnnotationSpec() {

    private var existingMember: Member? = null

    private var mongoLoadMemberAdapter = MongoLoadMemberAdapter(mongoTemplate)
//
    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    @BeforeEach
    fun setUp() {
        existingMember = createMember()
        existingMember = mongoTemplate.save(existingMember!!)
    }

    @Test
    fun `멤버 ID로 조회 테스트`() {
        // Given
        val memberId = existingMember?.id ?: throw IllegalStateException("Existing member not found")
        println("ExistingMember : {" +
                "id=${existingMember?.id}, " +
                "nickname=${existingMember?.nickname}, " +
                "oidcIdentities=${existingMember?.oidcIdentities}")
        // When
        val loadedMember = mongoLoadMemberAdapter.loadById(memberId)
        println("LoadedMember : {" +
                "id=${loadedMember?.id}, " +
                "nickname=${loadedMember?.nickname}, " +
                "oidcIdentities=${loadedMember?.oidcIdentities}")
        // Then
        loadedMember shouldNotBe null
        loadedMember?.id shouldBe memberId
        loadedMember?.nickname shouldBe existingMember?.nickname
        loadedMember?.oidcIdentities shouldNotBe null
        loadedMember?.oidcIdentities?.first()?.provider shouldBe existingMember?.oidcIdentities?.first()?.provider
        loadedMember?.oidcIdentities?.first()?.subject shouldBe existingMember?.oidcIdentities?.first()?.subject
        loadedMember?.isDeleted shouldBe false
        loadedMember?.createdAt shouldNotBe null
        loadedMember?.updatedAt shouldNotBe null
    }

    @Test
    fun `존재하지 않는 멤버 조회 테스트`() {
        val nonExistentId = "non-existent-id"
        val loadedMember = mongoLoadMemberAdapter.loadById(nonExistentId)
        loadedMember shouldBe null
    }
//
    @Test
    fun `Oidc 정보로 조회 테스트`() {
        // Given
        val oidcIdentity = existingMember?.oidcIdentities?.firstOrNull()
            ?: throw IllegalStateException("Existing member OidcIdentity not found")
        // When
        val loadedMember = mongoLoadMemberAdapter.loadByOidcIdentity(oidcIdentity)
        println("LoadedMember : {" +
                "id=${loadedMember?.id}, " +
                "nickname=${loadedMember?.nickname}, " +
                "oidcIdentities=${loadedMember?.oidcIdentities}," +
                "isDeleted = ${loadedMember?.isDeleted}"  +
        "")
        // Then
        loadedMember shouldNotBe null
        loadedMember?.oidcIdentities?.first()?.provider shouldBe oidcIdentity.provider
        loadedMember?.oidcIdentities?.first()?.subject shouldBe oidcIdentity.subject
    }

    @Test
    fun `존재하지 않는 Oidc 정보로 조회 테스트`() {
        // Given
        val nonExistentOidcIdentity = OidcIdentity(provider = "non-existent-provider", subject = "non-existent-subject")
        // When
        val loadedMember = mongoLoadMemberAdapter.loadByOidcIdentity(nonExistentOidcIdentity)
        // Then
        loadedMember shouldBe null
    }

    private fun createMember(
        name: String = "Test User"
    ): Member {
        return Member(
            nickname = name,
            oidcIdentities = mutableSetOf(
                OidcIdentity(
                    provider = "test-provider",
                    subject = "test-subject"
                )
            )// Assuming OidcIdentities are not needed for this test
        )
    }
}