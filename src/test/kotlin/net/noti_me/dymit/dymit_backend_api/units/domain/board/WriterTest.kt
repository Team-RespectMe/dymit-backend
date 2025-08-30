package net.noti_me.dymit.dymit_backend_api.units.domain.board

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import org.bson.types.ObjectId

/**
 * Writer 도메인 객체의 비즈니스 로직을 테스트하는 클래스
 */
class WriterTest : BehaviorSpec({

    given("StudyGroupMember 엔티티가 주어졌을 때") {
        val memberId = ObjectId()
        val groupId = ObjectId()
        val memberProfileImage = MemberProfileImageVo(
            type = "external",
            filePath = "path/to/profile.jpg",
            url = "https://example.com/profile.jpg",
            fileSize = 2048L,
            width = 150,
            height = 150
        )

        val studyGroupMember = StudyGroupMember(
            id = memberId,
            groupId = groupId,
            memberId = ObjectId(),
            nickname = "테스트사용자",
            profileImage = memberProfileImage,
            role = GroupMemberRole.MEMBER
        )

        `when`("Writer.from() 메서드를 통해 Writer 객체로 변환할 때") {
            val writer = Writer.from(studyGroupMember)

            then("Writer 객체의 id가 StudyGroupMember의 id와 동일해야 한다") {
                writer.id shouldBe studyGroupMember.id
            }

            then("Writer 객체의 nickname이 StudyGroupMember의 nickname과 동일해야 한다") {
                writer.nickname shouldBe studyGroupMember.nickname
            }

            then("Writer 객체의 image type이 StudyGroupMember의 profileImage type과 동일해야 한다") {
                writer.image.type shouldBe studyGroupMember.profileImage.type
            }

            then("Writer 객체의 image url이 StudyGroupMember의 profileImage url과 동일해야 한다") {
                writer.image.url shouldBe studyGroupMember.profileImage.url
            }

            then("변환된 Writer 객체가 올바른 모든 속성을 가져야 한다") {
                writer.id shouldBe studyGroupMember.id
                writer.nickname shouldBe "테스트사용자"
                writer.image.type shouldBe "external"
                writer.image.url shouldBe "https://example.com/profile.jpg"
            }
        }

        `when`("다른 프로필 이미지 타입을 가진 StudyGroupMember로 변환할 때") {
            val presetProfileImage = MemberProfileImageVo(
                type = "preset",
                filePath = "",
                url = "5",
                fileSize = 0L,
                width = 0,
                height = 0
            )

            val memberWithPresetImage = StudyGroupMember(
                id = ObjectId(),
                groupId = groupId,
                memberId = ObjectId(),
                nickname = "프리셋유저",
                profileImage = presetProfileImage,
                role = GroupMemberRole.ADMIN
            )

            val writer = Writer.from(memberWithPresetImage)

            then("preset 타입의 프로필 이미지가 올바르게 변환되어야 한다") {
                writer.image.type shouldBe "preset"
                writer.image.url shouldBe "5"
                writer.nickname shouldBe "프리셋유저"
            }
        }

        `when`("다양한 역할을 가진 StudyGroupMember들을 변환할 때") {
            val ownerMember = StudyGroupMember(
                id = ObjectId(),
                groupId = groupId,
                memberId = ObjectId(),
                nickname = "그룹장",
                profileImage = memberProfileImage,
                role = GroupMemberRole.OWNER
            )

            val adminMember = StudyGroupMember(
                id = ObjectId(),
                groupId = groupId,
                memberId = ObjectId(),
                nickname = "관리자",
                profileImage = memberProfileImage,
                role = GroupMemberRole.ADMIN
            )

            then("OWNER 역할의 멤버도 올바르게 Writer로 변환되어야 한다") {
                val ownerWriter = Writer.from(ownerMember)
                ownerWriter.nickname shouldBe "그룹장"
                ownerWriter.id shouldBe ownerMember.id
            }

            then("ADMIN 역할의 멤버도 올바르게 Writer로 변환되어야 한다") {
                val adminWriter = Writer.from(adminMember)
                adminWriter.nickname shouldBe "관리자"
                adminWriter.id shouldBe adminMember.id
            }
        }

        `when`("빈 닉네임을 가진 StudyGroupMember를 변환할 때") {
            val emptyNicknameMember = StudyGroupMember(
                id = ObjectId(),
                groupId = groupId,
                memberId = ObjectId(),
                nickname = "",
                profileImage = memberProfileImage,
                role = GroupMemberRole.MEMBER
            )

            val writer = Writer.from(emptyNicknameMember)

            then("빈 닉네임도 그대로 변환되어야 한다") {
                writer.nickname shouldBe ""
                writer.id shouldBe emptyNicknameMember.id
            }
        }
    }

    given("Writer 객체의 속성 변경 가능성을 확인할 때") {
        val studyGroupMember = StudyGroupMember(
            id = ObjectId(),
            groupId = ObjectId(),
            memberId = ObjectId(),
            nickname = "원본닉네임",
            profileImage = MemberProfileImageVo(
                type = "preset",
                filePath = "",
                url = "1",
                fileSize = 0L,
                width = 0,
                height = 0
            ),
            role = GroupMemberRole.MEMBER
        )

        `when`("Writer 객체가 생성된 후") {
            val writer = Writer.from(studyGroupMember)

            then("nickname 속성을 변경할 수 있어야 한다") {
                writer.nickname = "변경된닉네임"
                writer.nickname shouldBe "변경된닉네임"
            }

            then("image 속성을 변경할 수 있어야 한다") {
                val newImage = ProfileImageVo(type = "external", url = "new-url")
                writer.image = newImage
                writer.image.type shouldBe "external"
                writer.image.url shouldBe "new-url"
            }

            then("id 속성은 val이므로 변경할 수 없어야 한다") {
                // id는 val로 선언되어 있어 변경 불가
                val originalId = writer.id
                writer.id shouldBe originalId
            }
        }
    }
})
