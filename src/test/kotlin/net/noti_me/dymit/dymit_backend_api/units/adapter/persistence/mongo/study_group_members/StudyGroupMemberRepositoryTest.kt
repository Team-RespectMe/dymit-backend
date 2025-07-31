package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.study_group_members

import com.jayway.jsonpath.Criteria
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group_member.MongoStudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@Import(MongoConfig::class)
@DataMongoTest
class StudyGroupMemberRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private val repository = MongoStudyGroupMemberRepository(mongoTemplate)

    override fun extensions(): List<Extension> {
        return listOf(SpringExtension)
    }

    @BeforeEach
    fun setUp() {
        mongoTemplate.save(
            StudyGroupMember(
                groupId = "test-group-id",
                memberId = "test-member-id",
                nickname = "Test Member",
                profileImage = MemberProfileImageVo(
                    type = "preset",
                    filePath = "profile.jpg",
                    fileSize = 0,
                    url = "http://example.com/profile.jpg",
                    width = 100,
                    height = 100
                )
            )
        )
    }

    @Test
    fun `신규 회원 저장 테스트`() {
        val member = StudyGroupMember(
            groupId = "test-group-id",
            memberId = "test-member-id",
            nickname = "Test Member",
            profileImage = MemberProfileImageVo(type="preset", url = "1")
        )
        val savedMember = repository.persist(member)
        savedMember.groupId shouldBe "test-group-id"
        savedMember.memberId shouldBe "test-member-id"
        savedMember.nickname shouldBe "Test Member"
    }

    @Test
    fun `회원 조회 테스트`() {
        val member = repository.findByGroupIdAndMemberId("test-group-id", "test-member-id")
        member shouldNotBe  null
        member?.groupId shouldBe "test-group-id"
        member?.memberId shouldBe  "test-member-id"
        member?.nickname shouldBe "Test Member"
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(StudyGroupMember::class.java)
    }
    //
}