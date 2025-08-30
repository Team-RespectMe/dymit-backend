package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.study_group_members

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group_member.MongoStudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

@Import(MongoConfig::class)
@DataMongoTest
class StudyGroupMemberRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    private val repository = MongoStudyGroupMemberRepository(mongoTemplate)

    private var existMember : StudyGroupMember? = null

    override fun extensions(): List<Extension> {
        return listOf(SpringExtension)
    }

    @BeforeEach
    fun setUp() {
        existMember = StudyGroupMember(
            groupId = ObjectId(),
            memberId = ObjectId(),
            nickname = "testNickname",
            profileImage = MemberProfileImageVo(
                type = "preset",
                filePath = "profile.jpg",
                fileSize = 0,
                url = "http://example.com/profile.jpg",
                width = 100,
                height = 100
            )
        )
        mongoTemplate.save(existMember!!)
    }

    @Test
    fun `신규 회원 저장 테스트`() {
        val member = StudyGroupMember(
            groupId = ObjectId(),
            memberId = ObjectId(),
            nickname = "Test Member",
            profileImage = MemberProfileImageVo(type="preset", url = "1")
        )
        val savedMember = repository.persist(member)
        savedMember.groupId shouldBe member.groupId
        savedMember.memberId shouldBe member.memberId
        savedMember.nickname shouldBe "Test Member"
    }

    @Test
    fun `회원 조회 테스트`() {
        val member = repository.findByGroupIdAndMemberId(existMember!!.groupId, existMember!!.memberId)
        member shouldNotBe  null
        member?.groupId shouldBe existMember?.groupId
        member?.memberId shouldBe  existMember?.memberId
        member?.nickname shouldBe existMember?.nickname
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(StudyGroupMember::class.java)
    }
    //
}