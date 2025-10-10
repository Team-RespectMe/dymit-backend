package net.noti_me.dymit.dymit_backend_api.units.domain.study_schedule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * StudySchedule 도메인 엔티티 테스트
 * 스터디 그룹 일정 관리 기능을 테스트합니다.
 */
class StudyScheduleTest : BehaviorSpec({

    // 테스트용 헬퍼 함수들
    fun createTestMember(
        role: GroupMemberRole,
        groupId: ObjectId,
        memberId: ObjectId = ObjectId.get()
    ): StudyGroupMember {
        return StudyGroupMember(
            id = ObjectId.get(),
            groupId = groupId,
            memberId = memberId,
            nickname = "testUser_${role.name}",
            profileImage = ProfileImageVo(),
            role = role
        )
    }

    fun createFreshSchedule(
        groupId: ObjectId,
        scheduleAt: LocalDateTime = LocalDateTime.now().plusDays(1),
        title: String = "테스트 일정",
        description: String = "테스트 설명"
    ): StudySchedule {
        return StudySchedule(
            id = ObjectId.get(),
            groupId = groupId,
            title = title,
            description = description,
            location = ScheduleLocation(ScheduleLocation.LocationType.OFFLINE, "테스트 장소"),
            session = 1,
            scheduleAt = scheduleAt,
            roles = mutableSetOf(),
            nrParticipant = 0L
        )
    }

    fun createTestScheduleRole(memberId: ObjectId, roles: List<String> = listOf("발표자")): ScheduleRole {
        return ScheduleRole(
            memberId = memberId,
            nickname = "testUser",
            image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(),
            color = "#FF3357",
            roles = roles
        )
    }

    Given("StudySchedule 객체가 생성되었을 때") {
        val testGroupId = ObjectId.get()
        val schedule = createFreshSchedule(testGroupId)

        When("기본 속성들을 조회하면") {
            Then("올바른 값들이 반환된다") {
                schedule.groupId shouldBe testGroupId
                schedule.title shouldBe "테스트 일정"
                schedule.description shouldBe "테스트 설명"
                schedule.session shouldBe 1
                schedule.nrParticipant shouldBe 0L
                schedule.identifier shouldBe schedule.id.toHexString()
            }
        }
    }

    Given("제목 변경을 시도할 때") {
        val testGroupId = ObjectId.get()
        val ownerMember = createTestMember(GroupMemberRole.OWNER, testGroupId)
        val adminMember = createTestMember(GroupMemberRole.ADMIN, testGroupId)
        val regularMember = createTestMember(GroupMemberRole.MEMBER, testGroupId)
        val otherGroupOwner = createTestMember(GroupMemberRole.OWNER, ObjectId.get())

        When("권한이 있는 사용자(OWNER)가 유효한 제목으로 변경하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newTitle = "새로운 제목"

            Then("제목이 변경된다") {
                schedule.changeTitle(ownerMember, newTitle)
                schedule.title shouldBe newTitle
            }
        }

        When("권한이 있는 사용자(ADMIN)가 유효한 제목으로 변경하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newTitle = "관리자가 변경한 제목"

            Then("제목이 변경된다") {
                schedule.changeTitle(adminMember, newTitle)
                schedule.title shouldBe newTitle
            }
        }

        When("권한이 없는 사용자(MEMBER)가 제목 변경을 시도하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newTitle = "멤버가 변경하려는 제목"

            Then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    schedule.changeTitle(regularMember, newTitle)
                }
            }
        }

        When("다른 그룹의 사용자가 제목 변경을 시도하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newTitle = "다른 그룹 사용자가 변경하려는 제목"

            Then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    schedule.changeTitle(otherGroupOwner, newTitle)
                }
            }
        }

        When("30자를 초과하는 제목으로 변경하려고 하면") {
            val schedule = createFreshSchedule(testGroupId)
            val longTitle = "a".repeat(31)

            Then("IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    schedule.changeTitle(ownerMember, longTitle)
                }.message shouldBe "제목은 30자 이내로 작성해야 합니다."
            }
        }
    }

    Given("설명 변경을 시도할 때") {
        val testGroupId = ObjectId.get()
        val ownerMember = createTestMember(GroupMemberRole.OWNER, testGroupId)
        val regularMember = createTestMember(GroupMemberRole.MEMBER, testGroupId)

        When("권한이 있는 사용자가 유효한 설명으로 변경하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newDescription = "새로운 설명"

            Then("설명이 변경된다") {
                schedule.changeDescription(ownerMember, newDescription)
                schedule.description shouldBe newDescription
            }
        }

        When("권한이 없는 사용자가 설명 변경을 시도하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newDescription = "멤버가 변경하려는 설명"

            Then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    schedule.changeDescription(regularMember, newDescription)
                }
            }
        }

        When("100자를 초과하는 설명으로 변경하려고 하면") {
            val schedule = createFreshSchedule(testGroupId)
            val longDescription = "a".repeat(101)

            Then("IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    schedule.changeDescription(ownerMember, longDescription)
                }.message shouldBe "설명은 100자 이내로 작성해야 합니다."
            }
        }
    }

    Given("일정 시간 변경을 시도할 때") {
        val testGroupId = ObjectId.get()
        val ownerMember = createTestMember(GroupMemberRole.OWNER, testGroupId)
        val regularMember = createTestMember(GroupMemberRole.MEMBER, testGroupId)

        When("권한이 있는 사용자가 미래 시간으로 변경하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newScheduleAt = LocalDateTime.now().plusDays(3)

            Then("일정 시간이 변경되고 이벤트가 등록된다") {
                schedule.changeScheduleAt(ownerMember, newScheduleAt)
                schedule.scheduleAt shouldBe newScheduleAt
            }
        }

        When("권한이 없는 사용자가 시간 변경을 시도하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newScheduleAt = LocalDateTime.now().plusDays(2)

            Then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    schedule.changeScheduleAt(regularMember, newScheduleAt)
                }
            }
        }

        When("과거 시간으로 변경하려고 하면") {
            val schedule = createFreshSchedule(testGroupId)
            val pastTime = LocalDateTime.now().minusDays(1)

            Then("IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    schedule.changeScheduleAt(ownerMember, pastTime)
                }.message shouldBe "새로운 시간은 현재 시간 이후여야 합니다."
            }
        }

        When("이미 지나간 일정의 시간을 변경하려고 하면") {
            val pastSchedule = createFreshSchedule(
                groupId = testGroupId,
                scheduleAt = LocalDateTime.now().minusDays(1)
            )
            val newScheduleAt = LocalDateTime.now().plusDays(1)

            Then("IllegalArgumentException이 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    pastSchedule.changeScheduleAt(ownerMember, newScheduleAt)
                }.message shouldBe "이미 지나간 일정의 예정 시간은 변경할 수 없습니다."
            }
        }
    }

    Given("장소 변경을 시도할 때") {
        val testGroupId = ObjectId.get()
        val ownerMember = createTestMember(GroupMemberRole.OWNER, testGroupId)
        val regularMember = createTestMember(GroupMemberRole.MEMBER, testGroupId)

        When("권한이 있는 사용자가 다른 장소로 변경하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newLocation = ScheduleLocation(ScheduleLocation.LocationType.ONLINE, "https://zoom.us/test")

            Then("장소가 변경되고 이벤트가 등록된다") {
                schedule.changeLocation(ownerMember, newLocation)
                schedule.location shouldBe newLocation
                schedule.listDomainEvents().size shouldBe 1
//                schedule.listDomainEvents().first() shouldBe ScheduleTimeChangedEvent(schedule)
            }
        }

        When("권한이 없는 사용자가 장소 변경을 시도하면") {
            val schedule = createFreshSchedule(testGroupId)
            val newLocation = ScheduleLocation(ScheduleLocation.LocationType.ONLINE, "https://zoom.us/test")

            Then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    schedule.changeLocation(regularMember, newLocation)
                }
            }
        }

        When("동일한 장소로 변경하��고 하면") {
            val schedule = createFreshSchedule(testGroupId)
            val sameLocation = ScheduleLocation(
                schedule.location.type,
                schedule.location.value
            )

            Then("변경되지 않고 이벤트도 등록되지 않는다") {
                val originalLocation = schedule.location
                schedule.changeLocation(ownerMember, sameLocation)
                schedule.location shouldBe originalLocation
                schedule.listDomainEvents().size shouldBe 0
            }
        }
    }

    Given("역할 업데이트를 시도할 때") {
        val testGroupId = ObjectId.get()
        val ownerMember = createTestMember(GroupMemberRole.OWNER, testGroupId)
        val regularMember = createTestMember(GroupMemberRole.MEMBER, testGroupId)

        When("권한이 있는 사용자가 새로운 역할을 추가하면") {
            val schedule = createFreshSchedule(testGroupId)
            val member1Id = ObjectId.get()
            val member2Id = ObjectId.get()
            val member3Id = ObjectId.get()

            val initialRole1 = createTestScheduleRole(member1Id)
            val initialRole2 = createTestScheduleRole(member2Id)
            schedule.roles.addAll(setOf(initialRole1, initialRole2))

            val newRole = createTestScheduleRole(member3Id)
            val newRoles = setOf(initialRole1, initialRole2, newRole)

            Then("새로운 역할이 추가된다") {
                schedule.updateRoles(ownerMember, newRoles)
                schedule.roles.size shouldBe 3
                schedule.roles shouldBe newRoles
            }
        }

        When("기존 멤버의 역할을 변경하면") {
            val schedule = createFreshSchedule(testGroupId)
            val member1Id = ObjectId.get()
            val member2Id = ObjectId.get()

            val initialRole1 = createTestScheduleRole(member1Id)
            val initialRole2 = createTestScheduleRole(member2Id)
            schedule.roles.addAll(setOf(initialRole1, initialRole2))

            val updatedRole1 = ScheduleRole(
                memberId = member1Id,
                nickname = "testUser",
                image = net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo(),
                color = "#FF3357",
                roles = listOf("진행자", "발표자")
            )
            val newRoles = setOf(updatedRole1, initialRole2)

            Then("역할이 업데이트된다") {
                schedule.updateRoles(ownerMember, newRoles)
                schedule.roles.size shouldBe 2
                val updatedMember1Role = schedule.roles.find { it.memberId == member1Id }
                updatedMember1Role?.roles shouldBe listOf("진행자", "발표자")
            }
        }

        When("기존 멤버를 제거하면") {
            val schedule = createFreshSchedule(testGroupId)
            val member1Id = ObjectId.get()
            val member2Id = ObjectId.get()

            val initialRole1 = createTestScheduleRole(member1Id)
            val initialRole2 = createTestScheduleRole(member2Id)
            schedule.roles.addAll(setOf(initialRole1, initialRole2))

            val newRoles = setOf(initialRole1) // member2 제거

            Then("해당 멤버의 역할이 제거된다") {
                schedule.updateRoles(ownerMember, newRoles)
                schedule.roles.size shouldBe 1
                schedule.roles.find { it.memberId == member2Id } shouldBe null
                schedule.roles.find { it.memberId == member1Id } shouldNotBe null
            }
        }

        When("권한이 없는 사용자가 역할 업데이트를 시도하면") {
            val schedule = createFreshSchedule(testGroupId)
            val member1Id = ObjectId.get()
            val initialRole1 = createTestScheduleRole(member1Id)
            val newRoles = setOf(initialRole1)

            Then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    schedule.updateRoles(regularMember, newRoles)
                }
            }
        }
    }

    Given("참가자 수 관리를 할 때") {
        val testGroupId = ObjectId.get()

        When("참가자 수를 증가시키면") {
            val schedule = createFreshSchedule(testGroupId)

            Then("참가자 수가 1 증가한다") {
                val originalCount = schedule.nrParticipant
                schedule.increaseParticipantCount()
                schedule.nrParticipant shouldBe originalCount + 1
            }
        }

        When("참가자 수를 감소시키면") {
            val schedule = createFreshSchedule(testGroupId)
            // 먼저 참가자 수를 증가시켜 놓음
            schedule.increaseParticipantCount()
            schedule.increaseParticipantCount()
            val currentCount = schedule.nrParticipant

            Then("참가자 수가 1 감소한다") {
                schedule.decreaseParticipantCount()
                schedule.nrParticipant shouldBe currentCount - 1
            }
        }

        When("참가자 수가 0일 때 감소시키려고 하면") {
            val schedule = createFreshSchedule(testGroupId)

            Then("참가자 수는 0 이하로 내려가지 않는다") {
                schedule.decreaseParticipantCount()
                schedule.nrParticipant shouldBe 0
            }
        }
    }

    Given("equals와 hashCode 메서드를 테스트할 때") {
        When("같은 ID를 가진 스케줄을 비교하면") {
            val id1 = ObjectId.get()
            val schedule1 = StudySchedule(
                id = id1,
                scheduleAt = LocalDateTime.now().plusDays(1)
            )
            val schedule2 = StudySchedule(
                id = id1,
                scheduleAt = LocalDateTime.now().plusDays(2)
            )

            Then("equals가 true를 반환한다") {
                schedule1.equals(schedule2) shouldBe true
                schedule1.hashCode() shouldBe schedule2.hashCode()
            }
        }

        When("다른 ID를 가진 스케줄을 비교하면") {
            val id1 = ObjectId.get()
            val id2 = ObjectId.get()
            val schedule1 = StudySchedule(
                id = id1,
                scheduleAt = LocalDateTime.now().plusDays(1)
            )
            val schedule3 = StudySchedule(
                id = id2,
                scheduleAt = LocalDateTime.now().plusDays(1)
            )

            Then("equals가 false를 반환한다") {
                schedule1.equals(schedule3) shouldBe false
            }
        }

        When("자기 자신과 비교하면") {
            val schedule1 = StudySchedule(
                id = ObjectId.get(),
                scheduleAt = LocalDateTime.now().plusDays(1)
            )

            Then("equals가 true를 반환한다") {
                schedule1.equals(schedule1) shouldBe true
            }
        }

        When("null이나 다른 타입과 비교하면") {
            val schedule1 = StudySchedule(
                id = ObjectId.get(),
                scheduleAt = LocalDateTime.now().plusDays(1)
            )

            Then("equals가 false를 반환한다") {
                schedule1.equals(null) shouldBe false
                schedule1.equals("string") shouldBe false
            }
        }
    }
})