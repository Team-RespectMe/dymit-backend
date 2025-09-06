package net.noti_me.dymit.dymit_backend_api.units.domain.study_group

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.types.shouldBeInstanceOf
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentScheduleVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyGroupOwnerChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyGroupProfileImageDeleteEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * 스터디 그룹 도메인 엔티티 테스트 클래스
 * 이 클래스는 스터디 그룹 도메인 엔티티의 모든 기능을 테스트하기 위한 단위 테스트를 포함합니다.
 */
class StudyGroupTest : BehaviorSpec({

    // 테스트용 공통 ObjectId들
    val defaultOwnerId = ObjectId()
    val defaultMemberId = ObjectId()
    val defaultTargetMemberId = ObjectId()
    val defaultGroupId = ObjectId()

    // 테스트용 공통 문자열 상수들
    val defaultGroupName = "테스트 그룹"
    val defaultGroupDescription = "테스트 그룹 설명입니다"
    val defaultMemberNickname = "테스트멤버"
    val defaultOwnerNickname = "소유자"
    val defaultBlacklistReason = "부적절한 행동"
    val invalidObjectId = "invalid-object-id"
    val longName = "a".repeat(31)
    val maxLengthName = "a".repeat(30)
    val longDescription = "a".repeat(501)
    val maxLengthDescription = "a".repeat(500)
    val shortDescription = "1234"
    val validShortDescription = "12345"
    val eightCharCode = "12345678"
    val sevenCharCode = "1234567"
    val nineCharCode = "123456789"

    // 헬퍼 함수들
    fun createStudyGroup(
        id: ObjectId = ObjectId(), // 매번 새로운 ObjectId 생성
        ownerId: ObjectId = defaultOwnerId,
        name: String = defaultGroupName,
        description: String = defaultGroupDescription,
        memberCount: Int = 0,
        profileImage: GroupProfileImageVo = GroupProfileImageVo(),
        inviteCode: InviteCodeVo = InviteCodeVo(""),
        recentSchedule: RecentScheduleVo? = null,
        recentPost: RecentPostVo? = null
    ): StudyGroup = StudyGroup(
        id = id,
        ownerId = ownerId,
        name = name,
        description = description,
        memberCount = memberCount,
        profileImage = profileImage,
        inviteCode = inviteCode,
        recentSchedule = recentSchedule,
        recentPost = recentPost
    )

    fun createOwnerMember(
        groupId: ObjectId = ObjectId(), // 매번 새로운 ObjectId 생성
        memberId: ObjectId = defaultOwnerId,
        nickname: String = defaultOwnerNickname
    ): StudyGroupMember = StudyGroupMember(
        id = ObjectId(),
        groupId = groupId,
        memberId = memberId,
        nickname = nickname,
        profileImage = MemberProfileImageVo(),
        role = GroupMemberRole.OWNER
    )

    fun createRegularMember(
        groupId: ObjectId = ObjectId(), // 매번 새로운 ObjectId 생성
        memberId: ObjectId = defaultMemberId,
        nickname: String = defaultMemberNickname
    ): StudyGroupMember = StudyGroupMember(
        id = ObjectId(),
        groupId = groupId,
        memberId = memberId,
        nickname = nickname,
        profileImage = MemberProfileImageVo(),
        role = GroupMemberRole.MEMBER
    )

    fun createTargetMember(
        groupId: ObjectId = ObjectId(), // 매번 새로운 ObjectId 생성
        memberId: ObjectId = defaultTargetMemberId,
        nickname: String = "대상멤버"
    ): StudyGroupMember = StudyGroupMember(
        id = ObjectId(),
        groupId = groupId,
        memberId = memberId,
        nickname = nickname,
        profileImage = MemberProfileImageVo(),
        role = GroupMemberRole.MEMBER
    )

    fun createGroupProfileImage(
        filePath: String = "/path/to/image.jpg",
        type: String = "external",
        url: String = "https://example.com/image.jpg",
        fileSize: Long = 1024L,
        width: Int = 100,
        height: Int = 100
    ): GroupProfileImageVo = GroupProfileImageVo(
        filePath = filePath,
        type = type,
        url = url,
        fileSize = fileSize,
        width = width,
        height = height
    )

    fun createRecentSchedule(
        scheduleId: ObjectId = ObjectId(),
        title: String = "테스트 스케줄",
        session: Long = 1L,
        scheduleAt: LocalDateTime = LocalDateTime.now().plusDays(1)
    ): RecentScheduleVo = RecentScheduleVo(
        scheduleId = scheduleId,
        title = title,
        session = session,
        scheduleAt = scheduleAt
    )

    fun createRecentPost(
        postId: String = "test-post-id",
        title: String = "테스트 게시글",
        createdAt: LocalDateTime = LocalDateTime.now()
    ): RecentPostVo = RecentPostVo(
        postId = postId,
        title = title,
        createdAt = createdAt
    )

    // 시나리오별 헬퍼 함수들
    fun createStudyGroupWithOwner(ownerId: ObjectId = defaultOwnerId): StudyGroup =
        createStudyGroup(ownerId = ownerId)

    fun createStudyGroupWithMembers(): Triple<StudyGroup, StudyGroupMember, StudyGroupMember> {
        val studyGroup = createStudyGroup()
        val owner = createOwnerMember()
        val target = createTargetMember()
        return Triple(studyGroup, owner, target)
    }

    given("changeName() 메서드 테스트") {

        `when`("올바른 소유자가 유효한 이름으로 변경할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("그룹 이름이 성공적으로 변경되어야 한다") {
                studyGroup.changeName(defaultOwnerId.toHexString(), "새로운 그룹명")
                studyGroup.name shouldBe "새로운 그룹명"
            }
        }

        `when`("소유자가 아닌 사용자가 그룹 이름을 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.changeName(defaultMemberId.toHexString(), "새로운 그룹명")
                }
            }
        }

        `when`("빈 문자열로 그룹 이름을 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeName(defaultOwnerId.toHexString(), "")
                }
            }
        }

        `when`("공백만 있는 문자열로 그룹 이름을 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeName(defaultOwnerId.toHexString(), "   ")
                }
            }
        }

        `when`("30자를 초과하는 그룹 이름으로 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeName(defaultOwnerId.toHexString(), longName)
                }
            }
        }

        `when`("정확히 30자인 그룹 이름으로 변경할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeName(defaultOwnerId.toHexString(), maxLengthName)
                studyGroup.name shouldBe maxLengthName
            }
        }

        `when`("1자인 그룹 이름으로 변경할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeName(defaultOwnerId.toHexString(), "a")
                studyGroup.name shouldBe "a"
            }
        }
    }

    given("changeDescription() 메서드 테스트") {

        `when`("올바른 소유자가 유효한 설명으로 변경할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("그룹 설명이 성공적으로 변경되어야 한다") {
                studyGroup.changeDescription(defaultOwnerId.toHexString(), "새로운 설명입니다")
                studyGroup.description shouldBe "새로운 설명입니다"
            }
        }

        `when`("소유자가 아닌 사용자가 그룹 설명을 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.changeDescription(defaultMemberId.toHexString(), "새로운 설명입니다")
                }
            }
        }

        `when`("빈 문자열로 그룹 설명을 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(defaultOwnerId.toHexString(), "")
                }
            }
        }

        `when`("공백만 있는 문자열로 그룹 설명을 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(defaultOwnerId.toHexString(), "   ")
                }
            }
        }

        `when`("4자인 그룹 설명으로 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(defaultOwnerId.toHexString(), shortDescription)
                }
            }
        }

        `when`("500자를 초과하는 그룹 설명으로 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(defaultOwnerId.toHexString(), longDescription)
                }
            }
        }

        `when`("정확히 5자인 그룹 설명으로 변경할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeDescription(defaultOwnerId.toHexString(), validShortDescription)
                studyGroup.description shouldBe validShortDescription
            }
        }

        `when`("정확히 500자인 그룹 설명으로 변경할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeDescription(defaultOwnerId.toHexString(), maxLengthDescription)
                studyGroup.description shouldBe maxLengthDescription
            }
        }
    }

    given("changeOwner() 메서드 테스트") {

        `when`("올바른 소유자가 유효한 새 소유자로 변경할 때") {
            val studyGroup = createStudyGroupWithOwner()
            val newOwnerId = ObjectId()

            then("소유자가 성공적으로 변경되고 이벤트가 등록되어야 한다") {
                studyGroup.changeOwner(defaultOwnerId.toHexString(), newOwnerId.toHexString())
                studyGroup.ownerId shouldBe newOwnerId
                studyGroup.listDomainEvents() shouldHaveSize 1
                studyGroup.listDomainEvents().first().shouldBeInstanceOf<StudyGroupOwnerChangedEvent>()
            }
        }

        `when`("소유자가 아닌 사용자가 소유자를 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.changeOwner(defaultMemberId.toHexString(), ObjectId().toHexString())
                }
            }
        }

        `when`("유효하지 않은 ObjectId로 소유자를 변경하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeOwner(defaultOwnerId.toHexString(), invalidObjectId)
                }
            }
        }
    }

    given("updateProfileImage() 메서드 테스트") {

        `when`("올바른 소유자가 프로필 이미지를 업데이트할 때") {
            val studyGroup = createStudyGroupWithOwner()
            val newProfileImage = createGroupProfileImage()

            then("프로필 이미지가 성공적으로 업데이트되어야 한다") {
                studyGroup.updateProfileImage(defaultOwnerId.toHexString(), newProfileImage)
                studyGroup.profileImage shouldBe newProfileImage
            }
        }

        `when`("소유자가 아닌 사용자가 프로필 이미지를 업데이트하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()
            val newProfileImage = createGroupProfileImage()

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.updateProfileImage(defaultMemberId.toHexString(), newProfileImage)
                }
            }
        }
    }

    given("deleteProfileImage() 메서드 테스트") {

        `when`("external 타입의 프로필 이미지를 삭제할 때") {
            val studyGroup = createStudyGroup(
                profileImage = createGroupProfileImage(type = "external")
            )

            then("프로필 이미지가 preset으로 변경되고 삭제 이벤트가 등록되어야 한다") {
                studyGroup.deleteProfileImage(defaultOwnerId.toHexString())
                studyGroup.profileImage.type shouldBe "preset"
                studyGroup.profileImage.filePath shouldBe ""
                studyGroup.listDomainEvents() shouldHaveSize 1
                studyGroup.listDomainEvents().first().shouldBeInstanceOf<StudyGroupProfileImageDeleteEvent>()
            }
        }

        `when`("preset 타입의 프로필 이미지를 삭제할 때") {
            val studyGroup = createStudyGroup(
                profileImage = createGroupProfileImage(type = "preset", url = "1")
            )

            then("프로필 이미지가 새로운 preset으로 변경되고 삭제 이벤트는 등록되지 않아야 한다") {
                studyGroup.deleteProfileImage(defaultOwnerId.toHexString())
                studyGroup.profileImage.type shouldBe "preset"
                studyGroup.listDomainEvents() shouldHaveSize 0
            }
        }

        `when`("소유자가 아닌 사용자가 프로필 이미지를 삭제하려고 할 때") {
            val studyGroup = createStudyGroupWithOwner()

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.deleteProfileImage(defaultMemberId.toHexString())
                }
            }
        }
    }

    given("updateInviteCode() 메서드 테스트") {

        `when`("8자리 초대 코드로 업데이트할 때") {
            val studyGroup = createStudyGroup()

            then("초대 코드가 성공적으로 업데이트되어야 한다") {
                studyGroup.updateInviteCode(eightCharCode)
                studyGroup.inviteCode.code shouldBe eightCharCode
                studyGroup.inviteCode.createdAt shouldNotBe null
                studyGroup.inviteCode.expireAt shouldNotBe null
            }
        }

        `when`("8자가 아닌 초대 코드로 업데이트하려고 할 때") {
            val studyGroup = createStudyGroup()

            then("IllegalStateException이 발생해야 한다") {
                shouldThrow<IllegalStateException> {
                    studyGroup.updateInviteCode(sevenCharCode)
                }

                shouldThrow<IllegalStateException> {
                    studyGroup.updateInviteCode(nineCharCode)
                }
            }
        }
    }

    given("updateRecentSchedule() 메서드 테스트") {

        `when`("null로 최근 스케줄을 업데이트할 때") {
            val studyGroup = createStudyGroup(
                recentSchedule = createRecentSchedule(title = "기존 스케줄")
            )

            then("최근 스케줄이 null로 설정되어야 한다") {
                studyGroup.updateRecentSchedule(null)
                studyGroup.recentSchedule shouldBe null
            }
        }

        `when`("기존 최근 스케줄이 null일 때 새로운 스케줄을 추가할 때") {
            val studyGroup = createStudyGroup(recentSchedule = null)
            val newSchedule = createRecentSchedule(title = "새로운 스케줄")

            then("새로운 스케줄이 설정되어야 한다") {
                studyGroup.updateRecentSchedule(newSchedule)
                studyGroup.recentSchedule shouldBe newSchedule
            }
        }

        `when`("더 가까운 미래의 스케줄로 업데이트할 때") {
            val now = LocalDateTime.now()
            val existingSchedule = createRecentSchedule(
                title = "기존 스케줄",
                scheduleAt = now.plusDays(5)
            )
            val studyGroup = createStudyGroup(recentSchedule = existingSchedule)
            val nearerSchedule = createRecentSchedule(
                title = "더 가까운 스케줄",
                session = 2L,
                scheduleAt = now.plusDays(2)
            )

            then("더 가까운 스케줄로 업데이트되어야 한다") {
                studyGroup.updateRecentSchedule(nearerSchedule)
                studyGroup.recentSchedule shouldBe nearerSchedule
            }
        }

        `when`("더 먼 미래의 스케줄로 업데이트하려고 할 때") {
            val now = LocalDateTime.now()
            val existingSchedule = createRecentSchedule(
                title = "기존 스케줄",
                scheduleAt = now.plusDays(2)
            )
            val studyGroup = createStudyGroup(recentSchedule = existingSchedule)
            val fartherSchedule = createRecentSchedule(
                title = "더 먼 스케줄",
                session = 2L,
                scheduleAt = now.plusDays(5)
            )

            then("기존 스케줄이 유지되어야 한다") {
                studyGroup.updateRecentSchedule(fartherSchedule)
                studyGroup.recentSchedule shouldBe existingSchedule
            }
        }

        `when`("과거의 스케줄로 업데이트하려고 할 때") {
            val now = LocalDateTime.now()
            val existingSchedule = createRecentSchedule(
                title = "기존 스케줄",
                scheduleAt = now.plusDays(2)
            )
            val studyGroup = createStudyGroup(recentSchedule = existingSchedule)
            val pastSchedule = createRecentSchedule(
                title = "과거 스케줄",
                session = 2L,
                scheduleAt = now.minusDays(1)
            )

            then("기존 스케줄이 유지되어야 한다") {
                studyGroup.updateRecentSchedule(pastSchedule)
                studyGroup.recentSchedule shouldBe existingSchedule
            }
        }
    }

    given("updateRecentPost() 메서드 테스트") {

        `when`("null로 최근 게시글을 업데이트할 때") {
            val studyGroup = createStudyGroup(
                recentPost = createRecentPost(
                    postId = "existing-post",
                    title = "기존 게시글",
                    createdAt = LocalDateTime.now().minusHours(1)
                )
            )

            then("최근 게시글이 null로 설정되어야 한다") {
                studyGroup.updateRecentPost(null)
                studyGroup.recentPost shouldBe null
            }
        }

        `when`("기존 최근 게시글이 null일 때 새로운 게시글을 추가할 때") {
            val studyGroup = createStudyGroup(recentPost = null)
            val newPost = createRecentPost(postId = "new-post", title = "새로운 게시글")

            then("새로운 게시글이 설정되어야 한다") {
                studyGroup.updateRecentPost(newPost)
                studyGroup.recentPost shouldBe newPost
            }
        }

        `when`("더 최근 게시글로 업데이트할 때") {
            val now = LocalDateTime.now()
            val existingPost = createRecentPost(
                postId = "existing-post",
                title = "기존 게시글",
                createdAt = now.minusHours(2)
            )
            val studyGroup = createStudyGroup(recentPost = existingPost)
            val newerPost = createRecentPost(
                postId = "newer-post",
                title = "더 최근 게시글",
                createdAt = now.plusHours(1)
            )

            then("더 최근 게시글로 업데이트되어야 한다") {
                studyGroup.updateRecentPost(newerPost)
                studyGroup.recentPost shouldBe newerPost
            }
        }

        `when`("더 오래된 게시글로 업데이트하려고 할 때") {
            val now = LocalDateTime.now()
            val existingPost = createRecentPost(
                postId = "existing-post",
                title = "기존 게시글",
                createdAt = now.minusHours(1)
            )
            val studyGroup = createStudyGroup(recentPost = existingPost)
            val olderPost = createRecentPost(
                postId = "older-post",
                title = "더 오래된 게시글",
                createdAt = now.minusHours(2)
            )

            then("기존 게시글이 유지되어야 한다") {
                studyGroup.updateRecentPost(olderPost)
                studyGroup.recentPost shouldBe existingPost
            }
        }
    }

    given("equals() 및 hashCode() 메서드 테스트") {

        `when`("동일한 ID를 가진 두 StudyGroup 객체를 비교할 때") {
            val id = ObjectId()
            val studyGroup1 = createStudyGroup(id = id, name = "그룹1", description = "설명1")
            val studyGroup2 = createStudyGroup(id = id, name = "그룹2", description = "설명2")

            then("equals()는 true를 반환하고 hashCode()는 동일해야 한다") {
                studyGroup1.equals(studyGroup2) shouldBe true
                studyGroup1.hashCode() shouldBe studyGroup2.hashCode()
            }
        }

        `when`("다른 ID를 가진 두 StudyGroup 객체를 비교할 때") {
            val studyGroup1 = createStudyGroup(name = "그룹1", description = "설명1")
            println("studyGroup1 id: ${studyGroup1.id}")
            val studyGroup2 = createStudyGroup(name = "그룹1", description = "설명1")
            println("studyGroup2 id: ${studyGroup2.id}")
            then("equals()는 false를 반환해야 한다") {
                studyGroup1.equals(studyGroup2) shouldBe false
            }
        }

        `when`("같은 객체를 비교할 때") {
            val studyGroup = createStudyGroup()

            then("equals()는 true를 반환해야 한다") {
                studyGroup.equals(studyGroup) shouldBe true
            }
        }

        `when`("null과 비교할 때") {
            val studyGroup = createStudyGroup()

            then("equals()는 false를 반환해야 한다") {
                studyGroup.equals(null) shouldBe false
            }
        }

        `when`("다른 타입의 객체와 비교할 때") {
            val studyGroup = createStudyGroup()

            then("equals()는 false를 반환해야 한다") {
                studyGroup.equals("string") shouldBe false
            }
        }
    }

    given("StudyGroup 생성자 및 속성 테스트") {

        `when`("기본값으로 StudyGroup을 생성할 때") {
            val studyGroup = StudyGroup()

            then("모든 속성이 기본값으로 설정되어야 한다") {
                studyGroup.id shouldNotBe null
                studyGroup.ownerId shouldNotBe null
                studyGroup.name shouldBe ""
                studyGroup.description shouldBe ""
                studyGroup.memberCount shouldBe 0
                studyGroup.profileImage.type shouldBe "preset"
                studyGroup.inviteCode.code shouldBe ""
                studyGroup.recentSchedule shouldBe null
                studyGroup.recentPost shouldBe null
            }
        }

        `when`("모든 속성을 지정하여 StudyGroup을 생성할 때") {
            val id = ObjectId()
            val ownerId = ObjectId()
            val profileImage = createGroupProfileImage(type = "external")
            val inviteCode = InviteCodeVo(eightCharCode)
            val recentSchedule = createRecentSchedule()
            val recentPost = createRecentPost()

            val studyGroup = createStudyGroup(
                id = id,
                ownerId = ownerId,
                memberCount = 5,
                profileImage = profileImage,
                inviteCode = inviteCode,
                recentSchedule = recentSchedule,
                recentPost = recentPost
            )

            then("모든 속성이 올바르게 설정되어야 한다") {
                studyGroup.id shouldBe id
                studyGroup.ownerId shouldBe ownerId
                studyGroup.name shouldBe defaultGroupName
                studyGroup.description shouldBe defaultGroupDescription
                studyGroup.memberCount shouldBe 5
                studyGroup.profileImage shouldBe profileImage
                studyGroup.inviteCode shouldBe inviteCode
                studyGroup.recentSchedule shouldBe recentSchedule
                studyGroup.recentPost shouldBe recentPost
                studyGroup.identifier shouldBe id.toHexString()
            }
        }
    }

    given("addBlackList() 메서드 테스트") {

        `when`("그룹 소유자가 일반 멤버를 블랙리스트에 추가할 때") {
            val (studyGroup, owner, target) = createStudyGroupWithMembers()

            then("블랙리스트에 성공적으로 추가되고 이벤트가 등록되어야 한다") {
                studyGroup.addBlackList(owner, target, defaultBlacklistReason)
                studyGroup.isBlackListed(defaultTargetMemberId.toHexString()) shouldBe true
                studyGroup.listDomainEvents() shouldHaveSize 1
            }
        }

        `when`("일반 멤버가 다른 멤버를 블랙리스트에 추가하려고 할 때") {
            val (studyGroup, _, target) = createStudyGroupWithMembers()
            val regularMember = createRegularMember()

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.addBlackList(regularMember, target, defaultBlacklistReason)
                }
            }
        }

        `when`("그룹 소유자를 블랙리스트에 추가하려고 할 때") {
            val (studyGroup, owner, _) = createStudyGroupWithMembers()
            val anotherOwner = createOwnerMember(memberId = ObjectId())

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.addBlackList(owner, anotherOwner, defaultBlacklistReason)
                }
            }
        }

        `when`("본인을 블랙리스트에 추가하려고 할 때") {
            val studyGroup = createStudyGroup()
            val member = createRegularMember()

            then("ForbiddenException이 발생해야 한다 (권한 없음)") {
                shouldThrow<ForbiddenException> {
                    studyGroup.addBlackList(member, member, "테스트")
                }
            }
        }

        `when`("그룹 소유자가 본인을 블랙리스트에 추가하려고 할 때") {
            val studyGroup = createStudyGroup()
            val owner = createOwnerMember()

            then("ForbiddenException이 발생해야 한다 (소유자를 블랙리스트에 추가 불가)") {
                shouldThrow<ForbiddenException> {
                    studyGroup.addBlackList(owner, owner, "테스트")
                }
            }
        }

        `when`("빈 사유로 블랙리스트에 추가할 때") {
            val (studyGroup, owner, target) = createStudyGroupWithMembers()

            then("빈 사유로도 추가되어야 한다") {
                studyGroup.addBlackList(owner, target, "")
                studyGroup.isBlackListed(defaultTargetMemberId.toHexString()) shouldBe true
            }
        }

        `when`("이미 블랙리스트에 있는 멤버를 다시 추가하려고 ��� 때") {
            val (studyGroup, owner, target) = createStudyGroupWithMembers()

            studyGroup.addBlackList(owner, target, "첫 번째 사유")

            then("중복으로 추가되지 않아야 한다") {
                studyGroup.addBlackList(owner, target, "두 번째 사유")
                studyGroup.isBlackListed(defaultTargetMemberId.toHexString()) shouldBe true
            }
        }
    }

    given("removeBlackList() 메서드 테스트") {

        `when`("그룹 소유자가 블랙리스트에 있는 멤버를 제거할 때") {
            val (studyGroup, owner, target) = createStudyGroupWithMembers()
            studyGroup.addBlackList(owner, target, "테스트 사유")

            then("블랙리스트에서 성공적으로 제거되어야 한다") {
                studyGroup.removeBlackList(owner, defaultTargetMemberId.toHexString())
                studyGroup.isBlackListed(defaultTargetMemberId.toHexString()) shouldBe false
            }
        }

        `when`("일반 멤버가 블랙리스트에서 멤버를 제거하려고 할 때") {
            val studyGroup = createStudyGroup()
            val regularMember = createRegularMember()

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.removeBlackList(regularMember, defaultTargetMemberId.toHexString())
                }
            }
        }

        `when`("블랙리스트에 없는 멤버를 제거하려고 할 때") {
            val studyGroup = createStudyGroup()
            val owner = createOwnerMember()

            then("예외가 발생하지 않아야 한다") {
                studyGroup.removeBlackList(owner, defaultTargetMemberId.toHexString())
                studyGroup.isBlackListed(defaultTargetMemberId.toHexString()) shouldBe false
            }
        }

        `when`("유효하지 않은 ObjectId로 제거하려고 할 때") {
            val studyGroup = createStudyGroup()
            val owner = createOwnerMember()

            then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    studyGroup.removeBlackList(owner, invalidObjectId)
                }
            }
        }
    }

    given("isBlackListed() 메서드 테스트") {

        `when`("블랙리스트에 있는 멤버를 확인할 때") {
            val (studyGroup, owner, target) = createStudyGroupWithMembers()
            studyGroup.addBlackList(owner, target, "테스트 사유")

            then("true를 반환해야 한다") {
                studyGroup.isBlackListed(defaultTargetMemberId.toHexString()) shouldBe true
            }
        }

        `when`("블랙리스트에 없는 멤버를 확인할 때") {
            val studyGroup = createStudyGroup()

            then("false를 반환해야 한다") {
                studyGroup.isBlackListed(defaultMemberId.toHexString()) shouldBe false
            }
        }

        `when`("유효하지 않은 ObjectId로 확인하려고 할 때") {
            val studyGroup = createStudyGroup()

            then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    studyGroup.isBlackListed(invalidObjectId)
                }
            }
        }
    }
})
