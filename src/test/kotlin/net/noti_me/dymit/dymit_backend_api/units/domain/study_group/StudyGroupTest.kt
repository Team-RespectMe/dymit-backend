package net.noti_me.dymit.dymit_backend_api.units.domain.study_group

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.types.shouldBeInstanceOf
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentScheduleVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentPostVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyGroupOwnerChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyGroupProfileImageDeleteEvent
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * 스터디 그룹 도메인 엔티티 테스트 클래스
 * 이 클래스는 스터디 그룹 도메인 엔티티의 모든 기능을 테스트하기 위한 단위 테스트를 포함합니다.
 */
class StudyGroupTest : BehaviorSpec({

    given("changeName() 메서드 테스트") {

        `when`("올바른 소유자가 유효한 이름으로 변경할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "기존 그룹명",
                description = "기존 설명입니다"
            )

            then("그룹 이름이 성공적으로 변경되어야 한다") {
                studyGroup.changeName(ownerId.toHexString(), "새로운 그룹명")
                studyGroup.name shouldBe "새로운 그룹명"
            }
        }

        `when`("소유자가 아닌 사용자가 그룹 이름을 변경하려고 할 때") {
            val ownerId = ObjectId()
            val otherUserId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "기존 그룹명",
                description = "기존 설명입니다"
            )

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.changeName(otherUserId.toHexString(), "새로운 그룹명")
                }
            }
        }

        `when`("빈 문자열로 그룹 이름을 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "기존 그룹명",
                description = "기존 설명입니다"
            )

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeName(ownerId.toHexString(), "")
                }
            }
        }

        `when`("공백만 있는 문자열로 그룹 이름을 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "기존 그룹명",
                description = "기존 설명입니다"
            )

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeName(ownerId.toHexString(), "   ")
                }
            }
        }

        `when`("30자를 초과하는 그룹 이름으로 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "기존 그룹명",
                description = "기존 설명입니다"
            )
            val longName = "a".repeat(31)

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeName(ownerId.toHexString(), longName)
                }
            }
        }

        `when`("정확히 30자인 그룹 이름으로 변경할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "기존 그룹명",
                description = "기존 설명입니다"
            )
            val maxLengthName = "a".repeat(30)

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeName(ownerId.toHexString(), maxLengthName)
                studyGroup.name shouldBe maxLengthName
            }
        }

        `when`("1자인 그룹 이름으로 변경할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "기존 그룹명",
                description = "기존 설명입니다"
            )

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeName(ownerId.toHexString(), "a")
                studyGroup.name shouldBe "a"
            }
        }
    }

    given("changeDescription() 메서드 테스트") {

        `when`("올바른 소유자가 유효한 설명으로 변경할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )

            then("그룹 설명이 성공적으로 변경되어야 한다") {
                studyGroup.changeDescription(ownerId.toHexString(), "새로운 설명입니다")
                studyGroup.description shouldBe "새로운 설명입니다"
            }
        }

        `when`("소유자가 아닌 사용자가 그룹 설명을 변경하려고 할 때") {
            val ownerId = ObjectId()
            val otherUserId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.changeDescription(otherUserId.toHexString(), "새로운 설명입니다")
                }
            }
        }

        `when`("빈 문자열로 그룹 설명을 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(ownerId.toHexString(), "")
                }
            }
        }

        `when`("공백만 있는 문자열로 그룹 설명을 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(ownerId.toHexString(), "   ")
                }
            }
        }

        `when`("4자인 그룹 설명으로 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(ownerId.toHexString(), "1234")
                }
            }
        }

        `when`("500자를 초과하는 그룹 설명으로 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )
            val longDescription = "a".repeat(501)

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeDescription(ownerId.toHexString(), longDescription)
                }
            }
        }

        `when`("정확히 5자인 그룹 설명으로 변경할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeDescription(ownerId.toHexString(), "12345")
                studyGroup.description shouldBe "12345"
            }
        }

        `when`("정확히 500자인 그룹 설명으로 변경할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "기존 설명입니다"
            )
            val maxLengthDescription = "a".repeat(500)

            then("성공적으로 변경되어야 한다") {
                studyGroup.changeDescription(ownerId.toHexString(), maxLengthDescription)
                studyGroup.description shouldBe maxLengthDescription
            }
        }
    }

    given("changeOwner() 메서드 테스트") {

        `when`("올바른 소유자가 유효한 새 소유자로 변경할 때") {
            val ownerId = ObjectId()
            val newOwnerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다"
            )

            then("소유자가 성공적으로 변경되고 이벤트가 등록되어야 한다") {
                studyGroup.changeOwner(ownerId.toHexString(), newOwnerId.toHexString())
                studyGroup.ownerId shouldBe newOwnerId
                studyGroup.listDomainEvents() shouldHaveSize 1
                studyGroup.listDomainEvents().first().shouldBeInstanceOf<StudyGroupOwnerChangedEvent>()
            }
        }

        `when`("소유자가 아닌 사용자가 소유자를 변경하려고 할 때") {
            val ownerId = ObjectId()
            val otherUserId = ObjectId()
            val newOwnerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다"
            )

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.changeOwner(otherUserId.toHexString(), newOwnerId.toHexString())
                }
            }
        }

        `when`("유효하지 않은 ObjectId로 소유자를 변경하려고 할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다"
            )

            then("BadRequestException이 발생해야 한다") {
                shouldThrow<BadRequestException> {
                    studyGroup.changeOwner(ownerId.toHexString(), "invalid-object-id")
                }
            }
        }
    }

    given("updateProfileImage() 메서드 테스트") {

        `when`("올바른 소유자가 프로필 이미지를 업데이트할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다"
            )
            val newProfileImage = GroupProfileImageVo(
                filePath = "/path/to/image.jpg",
                type = "external",
                url = "https://example.com/image.jpg",
                fileSize = 1024L,
                width = 100,
                height = 100
            )

            then("프로필 이미지가 성공적으로 업데이트되어야 한다") {
                studyGroup.updateProfileImage(ownerId.toHexString(), newProfileImage)
                studyGroup.profileImage shouldBe newProfileImage
            }
        }

        `when`("소유자가 아닌 사용자가 프로필 이미지를 업데이트하려고 할 때") {
            val ownerId = ObjectId()
            val otherUserId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다"
            )
            val newProfileImage = GroupProfileImageVo(
                filePath = "/path/to/image.jpg",
                type = "external",
                url = "https://example.com/image.jpg"
            )

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.updateProfileImage(otherUserId.toHexString(), newProfileImage)
                }
            }
        }
    }

    given("deleteProfileImage() 메서드 테스트") {

        `when`("external 타입의 프로필 이미지를 삭제할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다",
                profileImage = GroupProfileImageVo(
                    filePath = "/path/to/image.jpg",
                    type = "external",
                    url = "https://example.com/image.jpg"
                )
            )

            then("프로필 이미지가 preset으로 변경되고 삭제 이벤트가 등록되어야 한다") {
                studyGroup.deleteProfileImage(ownerId.toHexString())
                studyGroup.profileImage.type shouldBe "preset"
                studyGroup.profileImage.filePath shouldBe ""
                studyGroup.listDomainEvents() shouldHaveSize 1
                studyGroup.listDomainEvents().first().shouldBeInstanceOf<StudyGroupProfileImageDeleteEvent>()
            }
        }

        `when`("preset 타입의 프로필 이미지를 삭제할 때") {
            val ownerId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다",
                profileImage = GroupProfileImageVo(
                    type = "preset",
                    url = "1"
                )
            )

            then("프로필 이미지가 새로운 preset으로 변경되고 삭제 이벤트는 등록되지 않아야 한다") {
                studyGroup.deleteProfileImage(ownerId.toHexString())
                studyGroup.profileImage.type shouldBe "preset"
                studyGroup.listDomainEvents() shouldHaveSize 0
            }
        }

        `when`("소유자가 아닌 사용자가 프로필 이미지를 삭제하려고 할 때") {
            val ownerId = ObjectId()
            val otherUserId = ObjectId()
            val studyGroup = StudyGroup(
                ownerId = ownerId,
                name = "그룹명",
                description = "그룹 설명입니다"
            )

            then("ForbiddenException이 발생해야 한다") {
                shouldThrow<ForbiddenException> {
                    studyGroup.deleteProfileImage(otherUserId.toHexString())
                }
            }
        }
    }

    given("updateInviteCode() 메서드 테스트") {

        `when`("8자리 초대 코드로 업데이트할 때") {
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다"
            )
            val newCode = "12345678"

            then("초대 코드가 성공적으로 업데이트되어야 한다") {
                studyGroup.updateInviteCode(newCode)
                studyGroup.inviteCode.code shouldBe newCode
                studyGroup.inviteCode.createdAt shouldNotBe null
                studyGroup.inviteCode.expireAt shouldNotBe null
            }
        }

        `when`("8자가 아닌 초대 코드로 업데이트하려고 할 때") {
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다"
            )

            then("IllegalStateException이 발생해야 한다") {
                shouldThrow<IllegalStateException> {
                    studyGroup.updateInviteCode("1234567")
                }

                shouldThrow<IllegalStateException> {
                    studyGroup.updateInviteCode("123456789")
                }
            }
        }
    }

    given("updateRecentSchedule() 메서드 테스트") {

        `when`("null로 최근 스케줄을 업데이트할 때") {
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentSchedule = RecentScheduleVo(
                    scheduleId = ObjectId(),
                    title = "기존 스케줄",
                    session = 1L,
                    scheduleAt = LocalDateTime.now().plusDays(1)
                )
            )

            then("최근 스케줄이 null로 설정되어야 한다") {
                studyGroup.updateRecentSchedule(null)
                studyGroup.recentSchedule shouldBe null
            }
        }

        `when`("기존 최근 스케줄이 null일 때 새로운 스케줄을 추가할 때") {
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentSchedule = null
            )
            val newSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
                title = "새로운 스케줄",
                session = 1L,
                scheduleAt = LocalDateTime.now().plusDays(1)
            )

            then("새로운 스케줄이 설정되어야 한다") {
                studyGroup.updateRecentSchedule(newSchedule)
                studyGroup.recentSchedule shouldBe newSchedule
            }
        }

        `when`("더 가까운 미래의 스케줄로 업데이트할 때") {
            val now = LocalDateTime.now()
            val existingSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
                title = "기존 스케줄",
                session = 1L,
                scheduleAt = now.plusDays(5)
            )
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentSchedule = existingSchedule
            )
            val nearerSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
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
            val existingSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
                title = "기존 스케줄",
                session = 1L,
                scheduleAt = now.plusDays(2)
            )
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentSchedule = existingSchedule
            )
            val fartherSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
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
            val existingSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
                title = "기존 스케줄",
                session = 1L,
                scheduleAt = now.plusDays(2)
            )
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentSchedule = existingSchedule
            )
            val pastSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
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
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentPost = RecentPostVo(
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
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentPost = null
            )
            val newPost = RecentPostVo(
                postId = "new-post",
                title = "새로운 게시글",
                createdAt = LocalDateTime.now()
            )

            then("새로운 게시글이 설정되어야 한다") {
                studyGroup.updateRecentPost(newPost)
                studyGroup.recentPost shouldBe newPost
            }
        }

        `when`("더 최근 게시글로 업데이트할 때") {
            val now = LocalDateTime.now()
            val existingPost = RecentPostVo(
                postId = "existing-post",
                title = "기존 게시글",
                createdAt = now.minusHours(2)
            )
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentPost = existingPost
            )
            val newerPost = RecentPostVo(
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
            val existingPost = RecentPostVo(
                postId = "existing-post",
                title = "기존 게시글",
                createdAt = now.minusHours(1)
            )
            val studyGroup = StudyGroup(
                name = "그룹명",
                description = "그룹 설명입니다",
                recentPost = existingPost
            )
            val olderPost = RecentPostVo(
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
            val studyGroup1 = StudyGroup(
                id = id,
                name = "그룹1",
                description = "설명1"
            )
            val studyGroup2 = StudyGroup(
                id = id,
                name = "그룹2",
                description = "설명2"
            )

            then("equals()는 true를 반환하고 hashCode()는 동일해야 한다") {
                studyGroup1.equals(studyGroup2) shouldBe true
                studyGroup1.hashCode() shouldBe studyGroup2.hashCode()
            }
        }

        `when`("다른 ID를 가진 두 StudyGroup 객체를 비교할 때") {
            val studyGroup1 = StudyGroup(
                id = ObjectId(),
                name = "그룹1",
                description = "설명1"
            )
            val studyGroup2 = StudyGroup(
                id = ObjectId(),
                name = "그룹1",
                description = "설명1"
            )

            then("equals()는 false를 반환해야 한다") {
                studyGroup1.equals(studyGroup2) shouldBe false
            }
        }

        `when`("같은 객체를 비교할 때") {
            val studyGroup = StudyGroup(
                name = "그룹",
                description = "설명"
            )

            then("equals()는 true를 반환해야 한다") {
                studyGroup.equals(studyGroup) shouldBe true
            }
        }

        `when`("null과 비교할 때") {
            val studyGroup = StudyGroup(
                name = "그룹",
                description = "설명"
            )

            then("equals()는 false를 반환해야 한다") {
                studyGroup.equals(null) shouldBe false
            }
        }

        `when`("다른 타입의 객체와 비교할 때") {
            val studyGroup = StudyGroup(
                name = "그룹",
                description = "설명"
            )

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
            val name = "테스트 그룹"
            val description = "테스트 그룹 설명입니다"
            val memberCount = 5
            val profileImage = GroupProfileImageVo(
                filePath = "/path/to/image.jpg",
                type = "external"
            )
            val inviteCode = InviteCodeVo("12345678")
            val recentSchedule = RecentScheduleVo(
                scheduleId = ObjectId(),
                title = "스케줄",
                session = 1L,
                scheduleAt = LocalDateTime.now().plusDays(1)
            )
            val recentPost = RecentPostVo(
                postId = "post-id",
                title = "게시글",
                createdAt = LocalDateTime.now()
            )

            val studyGroup = StudyGroup(
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

            then("모든 속성이 올바르게 설정되어야 한다") {
                studyGroup.id shouldBe id
                studyGroup.ownerId shouldBe ownerId
                studyGroup.name shouldBe name
                studyGroup.description shouldBe description
                studyGroup.memberCount shouldBe memberCount
                studyGroup.profileImage shouldBe profileImage
                studyGroup.inviteCode shouldBe inviteCode
                studyGroup.recentSchedule shouldBe recentSchedule
                studyGroup.recentPost shouldBe recentPost
                studyGroup.identifier shouldBe id.toHexString()
            }
        }
    }
})
