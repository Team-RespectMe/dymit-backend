package net.noti_me.dymit.dymit_backend_api.units.domain.studyGroup

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyGroupOwnerChangedEvent
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import org.bson.types.ObjectId
import java.util.UUID


/**
 * 스터디 그룹 도메인 엔티티 테스트 클래스
 * 이 클래스는 스터디 그룹 도메인 엔티티의 기능을 테스트하기 위한 단위 테스트를 포함합니다.
 * 테스트는 스터디 그룹 수정, 삭제 등의 기능을 검증합니다.
 */
class StudyGroupTest() : BehaviorSpec() {

    var studyGroup = createStudyGroup()

    init {
        beforeEach() {
            studyGroup = createStudyGroup()
        }

        given("스터디 그룹 이름을 수정한다.") {
            val newName = "New Study Group Name"
            `when`("요청 사용자가 그룹 사용자가 아닐 경우") {
                val userId = ObjectId.get().toHexString()
                then("ForbiddenException 예외가 발생한다.") {
                    val exception = shouldThrow<ForbiddenException> {
                        studyGroup.changeName(userId, newName)
                    }
                }
            }

            `when`("요청 사용자가 그룹 사용자이고, 새로운 이름이 유효한 경우") {
                then("스터디 그룹 이름이 변경된다.") {
                    val userId = studyGroup.ownerId
                    studyGroup.changeName(userId.toHexString(), newName)
                    studyGroup.name shouldBe newName
                }
            }

            `when`("요청 사용자가 그룹 사용자이고, 새로운 이름이 유효하지 않은 경우") {
                then("BadRequestException 예외가 발생한다.") {
                    val invalidName = "ab" // 길이가 3자 이상 30자 이하가 아니므로 예외 발생
                    val userId = studyGroup.ownerId
                    val exception = shouldThrow<BadRequestException> {
                        studyGroup.changeName(userId.toHexString(), invalidName)
                    }
                }
            }
        }

        given("스터디 그룹 설명을 변경한다.") {
            val newDescription = "New Study Group Description"
            `when`("요청 사용자가 그룹 사용자가 아닐 경우") {
                then("ForbiddenException 예외가 발생한다.") {
                    val userId = ObjectId.get().toHexString()
                    val exception = shouldThrow<ForbiddenException> {
                        studyGroup.changeDescription(userId, newDescription)
                    }
                }
            }

            `when`("요청 사용자가 그룹 사용자이고, 새로운 설명이 유효한 경우") {
                then("스터디 그룹 설명이 변경된다.") {
                    val userId = studyGroup.ownerId
                    studyGroup.changeDescription(userId.toHexString(), newDescription)
                    studyGroup.description shouldBe newDescription
                }
            }

            `when`("요청 사용자가 그룹 사용자이고, 새로운 설명이 유효하지 않은 경우") {
                then("BadRequestException 예외가 발생한다.") {
                    val userId = studyGroup.ownerId
                    val invalidDescription = "ab" // 길이가 5자 이상 500자 이하가 아니므로 예외 발생
                    val exception = shouldThrow<BadRequestException> {
                        studyGroup.changeDescription(userId.toHexString(), invalidDescription)
                    }
                }
            }
        }

        given("스터디 그룹 소유자를 변경한다.") {
            `when`("요청 사용자가 그룹 소유자가 아닐 경우") {

                then("ForbiddenException 예외가 발생한다.") {
                    val newOwnerId = ObjectId.get().toHexString()
                    val userId = ObjectId.get().toHexString()
                    val exception = shouldThrow<ForbiddenException> {
                        studyGroup.changeOwner(userId, newOwnerId)
                    }
                }
            }

            `when`("요청 사용자가 그룹 소유자이고, 새로운 소유자 ID가 유효한 경우") {
                then("스터디 그룹 소유자가 변경되고, GroupOwnerChangedEvent 이벤트가 발생한다.") {
                    val userId = studyGroup.ownerId
                    val newOwnerId = ObjectId.get().toHexString()
                    studyGroup.changeOwner(userId.toHexString(), newOwnerId)
                    studyGroup.ownerId.toHexString() shouldBe newOwnerId
                    studyGroup.listDomainEvents().size shouldBe 1
                    val event = studyGroup.listDomainEvents().first()
                    event::class shouldBe StudyGroupOwnerChangedEvent::class
                }
            }
        }

        given("프로필 이미지를 변경한다.") {
            val newProfileImage = createProfileImage()
            `when`("요청 사용자가 그룹 소유자가 아닐 경우") {
                val userId = "non-owner-id"
                then("ForbiddenException 예외가 발생한다.") {
                    val exception = shouldThrow<ForbiddenException> {
                        studyGroup.updateProfileImage(userId, newProfileImage)
                    }
                }
            }

            `when`("요청 사용자가 그룹 소유자이고, 새로운 프로필 이미지가 유효한 경우") {
                then("프로필 이미지가 변경된다.") {
                    val userId = studyGroup.ownerId
                    studyGroup.updateProfileImage(userId.toHexString(), newProfileImage)
                    studyGroup.profileImage shouldBe newProfileImage
                }
            }   

            `when`("요청 사용자가 그룹 소유자이고, 새로운 프로필 이미지가 유효하지 않은 경우") {
                then("BadRequestException 예외가 발생한다.") {
                    val userId = studyGroup.ownerId
                    val invalidProfileImage = createProfileImage(fileSize = 0L)
                    val exception = shouldThrow<BadRequestException> {
                        studyGroup.updateProfileImage(userId.toHexString(), invalidProfileImage)
                    }
                }
            }
        }

        afterEach() {
            clearAllMocks()
        }
    }

    fun createStudyGroup(): StudyGroup {
        return StudyGroup(
            name = "Test Study Group",
            description = "This is a test study group.",
            profileImage = createProfileImage(),
        )
    }

    fun createProfileImage(
        fileSize: Long = 1024L
    ): GroupProfileImageVo {
        val id = UUID.randomUUID().toString()
        return GroupProfileImageVo(
            filePath = "path/to/${id}.jpg",
            url = "https://cdn.example.com/${id}.jpg",
            fileSize = fileSize,
            width = 100,
            height = 100
        )
    }

}