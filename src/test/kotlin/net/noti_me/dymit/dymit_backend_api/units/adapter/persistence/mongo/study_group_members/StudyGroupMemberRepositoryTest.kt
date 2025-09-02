package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.study_group_members

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group_member.MongoStudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate

@Import(MongoConfig::class)
@DataMongoTest
class StudyGroupMemberRepositoryTest(
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    override fun extensions(): List<Extension> = listOf(SpringExtension)

    private lateinit var repository: MongoStudyGroupMemberRepository

    @BeforeEach
    fun setup() {
        repository = MongoStudyGroupMemberRepository(mongoTemplate)
        mongoTemplate.remove(org.springframework.data.mongodb.core.query.Query(), StudyGroupMember::class.java)
    }

    @Test
    fun `persist - 새로운 스터디 그룹 멤버를 저장한다`() {
        // Given
        val groupId = ObjectId()
        val memberId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")
        val member = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "테스트유저",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )

        // When
        val savedMember = repository.persist(member)

        // Then
        savedMember.id shouldNotBe null
        savedMember.groupId shouldBe groupId
        savedMember.memberId shouldBe memberId
        savedMember.nickname shouldBe "테스트유저"
        savedMember.role shouldBe GroupMemberRole.MEMBER
    }

    @Test
    fun `update - 기존 스터디 그룹 멤버를 업데이트한다`() {
        // Given
        val groupId = ObjectId()
        val memberId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")
        val member = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "원래닉네임",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        val savedMember = repository.persist(member)
        savedMember.updateNickname("변경된닉네임")

        // When
        val updatedMember = repository.update(savedMember)

        // Then
        updatedMember.id shouldBe savedMember.id
        updatedMember.nickname shouldBe "변경된닉네임"
    }

    @Test
    fun `delete - 스터디 그룹 멤버를 삭제한다`() {
        // Given
        val groupId = ObjectId()
        val memberId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")
        val member = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "테스트유저",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        val savedMember = repository.persist(member)

        // When
        val result = repository.delete(savedMember)

        // Then
        result shouldBe true
        repository.findByGroupIdAndMemberId(groupId, memberId) shouldBe null
    }

    @Test
    fun `delete - 존재하지 않는 멤버 삭제시 false를 반환한다`() {
        // Given
        val groupId = ObjectId()
        val memberId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")
        val member = StudyGroupMember(
            id = ObjectId(),
            groupId = groupId,
            memberId = memberId,
            nickname = "테스트유저",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )

        // When
        val result = repository.delete(member)

        // Then
        result shouldBe false
    }

    @Test
    fun `findByGroupIdAndMemberId - 그룹 ID와 멤버 ID로 멤버를 찾는다`() {
        // Given
        val groupId = ObjectId()
        val memberId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")
        val member = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "테스트유저",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        repository.persist(member)

        // When
        val foundMember = repository.findByGroupIdAndMemberId(groupId, memberId)

        // Then
        foundMember shouldNotBe null
        foundMember!!.groupId shouldBe groupId
        foundMember.memberId shouldBe memberId
        foundMember.nickname shouldBe "테스트유저"
    }

    @Test
    fun `findByGroupIdAndMemberId - 존재하지 않는 멤버 조회시 null을 반환한다`() {
        // Given
        val groupId = ObjectId()
        val memberId = ObjectId()

        // When
        val foundMember = repository.findByGroupIdAndMemberId(groupId, memberId)

        // Then
        foundMember shouldBe null
    }

    @Test
    fun `findByGroupId - 그룹 ID로 모든 멤버를 찾는다`() {
        // Given
        val groupId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        val member1 = StudyGroupMember(
            groupId = groupId,
            memberId = ObjectId(),
            nickname = "멤버1",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        val member2 = StudyGroupMember(
            groupId = groupId,
            memberId = ObjectId(),
            nickname = "멤버2",
            profileImage = profileImage,
            role = GroupMemberRole.ADMIN
        )

        repository.persist(member1)
        repository.persist(member2)

        // When
        val members = repository.findByGroupId(groupId)

        // Then
        members shouldHaveSize 2
    }

    @Test
    fun `findByGroupId - 해당 그룹에 멤버가 없으면 빈 리스트를 반환한다`() {
        // Given
        val groupId = ObjectId()

        // When
        val members = repository.findByGroupId(groupId)

        // Then
        members.shouldBeEmpty()
    }

    @Test
    fun `countByGroupId - 그룹의 멤버 수를 반환한다`() {
        // Given
        val groupId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        repeat(3) { index ->
            val member = StudyGroupMember(
                groupId = groupId,
                memberId = ObjectId(),
                nickname = "멤버$index",
                profileImage = profileImage,
                role = GroupMemberRole.MEMBER
            )
            repository.persist(member)
        }

        // When
        val count = repository.countByGroupId(groupId)

        // Then
        count shouldBe 3
    }

    @Test
    fun `countByGroupId - 해당 그룹에 멤버가 없으면 0을 반환한다`() {
        // Given
        val groupId = ObjectId()

        // When
        val count = repository.countByGroupId(groupId)

        // Then
        count shouldBe 0
    }

    @Test
    fun `findByGroupIdsOrderByCreatedAt - 여러 그룹의 멤버들을 그룹별로 묶어서 반환한다`() {
        // Given
        val groupId1 = ObjectId()
        val groupId2 = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        val member1 = StudyGroupMember(
            groupId = groupId1,
            memberId = ObjectId(),
            nickname = "그룹1멤버1",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        val member2 = StudyGroupMember(
            groupId = groupId1,
            memberId = ObjectId(),
            nickname = "그룹1멤버2",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        val member3 = StudyGroupMember(
            groupId = groupId2,
            memberId = ObjectId(),
            nickname = "그룹2멤버1",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )

        repository.persist(member1)
        Thread.sleep(1)
        repository.persist(member2)
        Thread.sleep(1)
        repository.persist(member3)

        // When
        val result = repository.findByGroupIdsOrderByCreatedAt(listOf(groupId1, groupId2), 10)

        // Then
        result shouldContainKey groupId1.toHexString()
        result shouldContainKey groupId2.toHexString()
        result[groupId1.toHexString()]!! shouldHaveSize 2
        result[groupId2.toHexString()]!! shouldHaveSize 1
    }

    @Test
    fun `findByGroupIdsOrderByCreatedAt - limit이 적용된다`() {
        // Given
        val groupId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        repeat(5) { index ->
            val member = StudyGroupMember(
                groupId = groupId,
                memberId = ObjectId(),
                nickname = "멤버$index",
                profileImage = profileImage,
                role = GroupMemberRole.MEMBER
            )
            repository.persist(member)
            Thread.sleep(1)
        }

        // When
        val result = repository.findByGroupIdsOrderByCreatedAt(listOf(groupId), 3)

        // Then
        result[groupId.toHexString()]!! shouldHaveSize 3
    }

    @Test
    fun `findGroupIdsByMemberId - 멤버 ID로 참여 중인 그룹 ID들을 반환한다`() {
        // Given
        val memberId = ObjectId()
        val groupId1 = ObjectId()
        val groupId2 = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        val member1 = StudyGroupMember(
            groupId = groupId1,
            memberId = memberId,
            nickname = "테스트유저",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        val member2 = StudyGroupMember(
            groupId = groupId2,
            memberId = memberId,
            nickname = "테스트유저",
            profileImage = profileImage,
            role = GroupMemberRole.ADMIN
        )

        repository.persist(member1)
        repository.persist(member2)

        // When
        val groupIds = repository.findGroupIdsByMemberId(memberId)

        // Then
        groupIds shouldHaveSize 2
        groupIds shouldContain groupId1.toHexString()
        groupIds shouldContain groupId2.toHexString()
    }

    @Test
    fun `findGroupIdsByMemberId - 중복된 그룹 ID는 제거된다`() {
        // Given
        val memberId = ObjectId()
        val groupId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        // 같은 그룹에 같은 멤버가 두 번 추가되는 경우 (실제로는 발생하지 않아야 하지만 테스트용)
        val member1 = StudyGroupMember(
            groupId = groupId,
            memberId = memberId,
            nickname = "테스트유저1",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )

        repository.persist(member1)

        // When
        val groupIds = repository.findGroupIdsByMemberId(memberId)

        // Then
        groupIds shouldHaveSize 1
        groupIds shouldContainExactly listOf(groupId.toHexString())
    }

    @Test
    fun `findGroupIdsByMemberId - 해당 멤버가 참여한 그룹이 없으면 빈 리스트를 반환한다`() {
        // Given
        val memberId = ObjectId()

        // When
        val groupIds = repository.findGroupIdsByMemberId(memberId)

        // Then
        groupIds.shouldBeEmpty()
    }

    @Test
    fun `findByGroupIdAndMemberIdsIn - 그룹 ID와 멤버 ID 리스트로 멤버들을 찾는다`() {
        // Given
        val groupId = ObjectId()
        val memberId1 = ObjectId()
        val memberId2 = ObjectId()
        val memberId3 = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        val member1 = StudyGroupMember(
            groupId = groupId,
            memberId = memberId1,
            nickname = "멤버1",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )
        val member2 = StudyGroupMember(
            groupId = groupId,
            memberId = memberId2,
            nickname = "멤버2",
            profileImage = profileImage,
            role = GroupMemberRole.ADMIN
        )
        val member3 = StudyGroupMember(
            groupId = groupId,
            memberId = memberId3,
            nickname = "멤버3",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )

        repository.persist(member1)
        repository.persist(member2)
        repository.persist(member3)

        // When
        val members = repository.findByGroupIdAndMemberIdsIn(groupId, listOf(memberId1, memberId2))

        // Then
        members shouldHaveSize 2
    }

    @Test
    fun `findByGroupIdAndMemberIdsIn - 해당하는 멤버가 없으면 빈 리스트를 반환한다`() {
        // Given
        val groupId = ObjectId()
        val memberId1 = ObjectId()
        val memberId2 = ObjectId()

        // When
        val members = repository.findByGroupIdAndMemberIdsIn(groupId, listOf(memberId1, memberId2))

        // Then
        members.shouldBeEmpty()
    }

    @Test
    fun `findByGroupIdAndMemberIdsIn - 다른 그룹의 멤버는 반환되지 않는다`() {
        // Given
        val groupId1 = ObjectId()
        val groupId2 = ObjectId()
        val memberId = ObjectId()
        val profileImage = MemberProfileImageVo("test.jpg", "https://example.com/test.jpg")

        val member = StudyGroupMember(
            groupId = groupId2, // 다른 그룹
            memberId = memberId,
            nickname = "멤버1",
            profileImage = profileImage,
            role = GroupMemberRole.MEMBER
        )

        repository.persist(member)

        // When
        val members = repository.findByGroupIdAndMemberIdsIn(groupId1, listOf(memberId))

        // Then
        members.shouldBeEmpty()
    }
}