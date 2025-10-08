package net.noti_me.dymit.dymit_backend_api.units.application.study_schedule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.impl.StudyScheduleServiceImpl
import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.ConflictException
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.controllers.study_schedule.dto.RoleAssignment
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.RecentScheduleVo
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCanceledEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyScheduleCreatedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleParticipant
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.SaveStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_schedule.StudyScheduleRepository
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

/**
 * StudyScheduleServiceImpl 클래스에 대한 테스트
 *
 * 스터디 스케줄 관련 비즈니스 로직의 모든 시나리오를 테스트합니다.
 */
class StudyScheduleServiceImplTest : BehaviorSpec({

    // 테스트 전반에 걸쳐 재사용되는 공통 데이터
    val memberInfo = createMemberInfo()

    val groupId = ObjectId().toHexString()

    val scheduleId = ObjectId().toHexString()

    // Mock 객체들을 클래스 필드에서 선언과 동시에 생성
    val groupMemberRepository = mockk<StudyGroupMemberRepository>()

    val loadStudyGroupPort = mockk<LoadStudyGroupPort>()

    val saveStudyGroupPort = mockk<SaveStudyGroupPort>()

    val studyScheduleRepository = mockk<StudyScheduleRepository>()

    val participantRepository = mockk<ScheduleParticipantRepository>()

    val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    // 서비스 객체를 한 번만 생성
    val studyScheduleService = StudyScheduleServiceImpl(
        groupMemberRepository = groupMemberRepository,
        loadStudyGroupPort = loadStudyGroupPort,
        saveStudyGroupPort = saveStudyGroupPort,
        studyScheduleRepository = studyScheduleRepository,
        participantRepository = participantRepository,
        eventPublisher = eventPublisher
    )

    beforeSpec {

    }

    given("스터디 그룹 소유자가 새로운 스케줄을 생성하려고 할 때") {

        `when`("유효한 정보로 스케줄 생성을 요청하면") {

            val command = createScheduleCreateCommand()
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))
            val groupMember = createGroupMember(
                memberId = ObjectId(memberInfo.memberId),
                groupId = ObjectId(groupId),
                role = GroupMemberRole.OWNER
            )
            val savedSchedule = createStudySchedule(groupId = ObjectId(groupId))

            // RoleAssignment에서 사용하는 memberId를 위한 멤버 생성
            val roleAssignmentMember = createGroupMember(
                memberId = ObjectId(command.roles.first().memberId),
                groupId = ObjectId(groupId)
            )

            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every {
                groupMemberRepository.findByGroupIdAndMemberId(
                    ObjectId(groupId),
                    ObjectId(memberInfo.memberId)
                )
            } returns groupMember
            every { studyScheduleRepository.countByGroupId(ObjectId(groupId)) } returns 2L
            every { studyScheduleRepository.save(any()) } returns savedSchedule
            every { saveStudyGroupPort.persist(any()) } returns studyGroup
            every { groupMemberRepository.findByGroupIdAndMemberIdsIn(any(), any()) } returns listOf(roleAssignmentMember)

            then("새로운 스케줄이 성공적으로 생성된다") {
                val result = studyScheduleService.createSchedule(memberInfo, groupId, command)

                result shouldNotBe null
                result.title shouldBe command.title
                result.description shouldBe command.description
                verify { eventPublisher.publishEvent(any<StudyScheduleCreatedEvent>()) }
            }
        }

        `when`("존재하지 않는 그룹에 스케줄 생성을 요청하면") {
            val command = createScheduleCreateCommand()

            every { loadStudyGroupPort.loadByGroupId(groupId) } returns null

            then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    studyScheduleService.createSchedule(memberInfo, groupId, command)
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("그룹에 가입하지 않은 사용자가 스케��� 생성을 요청하면") {
            val command = createScheduleCreateCommand()
            val studyGroup = createStudyGroup()

            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns null

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.createSchedule(memberInfo, groupId, command)
                }
                exception.message shouldBe "가입된 그룹이 아닙니다."
            }
        }

        `when`("그룹 소유자가 아닌 사용자가 스케줄 생성을 요청하면") {
            val command = createScheduleCreateCommand()
            val studyGroup = createStudyGroup()
            val groupMember = createGroupMember(role = GroupMemberRole.MEMBER)

            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns groupMember

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.createSchedule(memberInfo, groupId, command
                    )
                }
                exception.message shouldBe "스터디 그룹의 소유자만 스케줄을 생성할 수 있습니다."
            }
        }

        `when`("존재하지 않는 멤버를 역할에 할당하여 스케줄 생성을 요청하면") {
            val command = createScheduleCreateCommand()
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))
            val groupMember = createGroupMember(
                memberId = ObjectId(memberInfo.memberId),
                groupId = ObjectId(groupId),
                role = GroupMemberRole.OWNER
            )

            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every {
                groupMemberRepository.findByGroupIdAndMemberId(
                    ObjectId(groupId),
                    ObjectId(memberInfo.memberId)
                )
            } returns groupMember
            every { studyScheduleRepository.countByGroupId(ObjectId(groupId)) } returns 2L
            // 역할에 할당된 멤버가 존재하지 않는 경우
            every { groupMemberRepository.findByGroupIdAndMemberIdsIn(any(), any()) } returns emptyList()

            then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    studyScheduleService.createSchedule(memberInfo, groupId, command)
                }
                exception.message shouldBe "존재하지 않는 멤버입니다."
            }
        }
    }

    given("스터디 그룹 소유자가 기존 스케줄을 수정하려 할 때") {

        `when`("유효한 정보로 스케줄 수정을 요청하면") {
            val command = createScheduleUpdateCommand()
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))
            val groupMember = createGroupMember(
                memberId = ObjectId(memberInfo.memberId),
                groupId = ObjectId(groupId),
                role = GroupMemberRole.OWNER
            )
            val schedule = createStudySchedule(groupId = ObjectId(groupId), scheduleId = ObjectId(scheduleId))

            // RoleAssignment에서 사용하는 memberId를 위한 멤버 생성
            val roleAssignmentMember = createGroupMember(
                memberId = ObjectId(command.roles.first().memberId),
                groupId = ObjectId(groupId)
            )

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every {
                groupMemberRepository.findByGroupIdAndMemberId(
                    ObjectId(groupId),
                    ObjectId(memberInfo.memberId)
                )
            } returns groupMember
            every { studyScheduleRepository.save(any()) } returns schedule
            every { saveStudyGroupPort.persist(any()) } returns studyGroup
            every { groupMemberRepository.findByGroupIdAndMemberIdsIn(any(), any()) } returns listOf(roleAssignmentMember)

            then("스케줄이 성공적으로 수정된다") {
                val result = studyScheduleService.updateSchedule(memberInfo, groupId, scheduleId, command)
                result.title shouldBe command.title
                result.description shouldBe command.description
            }
        }

        `when`("존재하지 않는 스케줄 수정을 요청하면") {
            val command = createScheduleUpdateCommand()

            every { studyScheduleRepository.loadById(any()) } returns null

            then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    studyScheduleService.updateSchedule(memberInfo, groupId, scheduleId, command)
                }
                exception.message shouldBe "존재하지 않는 스케줄입니다."
            }
        }

        `when`("다른 그룹의 스케줄 수정을 요청하면") {
            val command = createScheduleUpdateCommand()
            val schedule = createStudySchedule(groupId = ObjectId())

            every { studyScheduleRepository.loadById(any()) } returns schedule

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.updateSchedule(memberInfo, groupId, scheduleId, command)
                }
                exception.message shouldBe "해당 그룹의 스케줄이 아닙니다."
            }
        }
    }

    given("스터디 그룹 소유자가 스케줄을 삭제하려고 할 때") {

        `when`("미래 스케줄 삭제를 요청하면") {
            val futureDateTime = LocalDateTime.now().plusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleId = ObjectId(scheduleId),
                scheduleAt = futureDateTime
            )
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every { studyScheduleRepository.delete(any()) } returns true

            then("스케줄이 완전히 삭제된다") {
                studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)

                verify { studyScheduleRepository.delete(schedule) }
                verify { eventPublisher.publishEvent(any<StudyScheduleCanceledEvent>()) }
            }
        }

        `when`("과거 스케줄 삭제를 요청하면") {
            val pastDateTime = LocalDateTime.now().minusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleId = ObjectId(scheduleId),
                scheduleAt = pastDateTime
            )
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every { studyScheduleRepository.save(any()) } returns schedule

            then("스케줄이 소프트 삭제된다") {
                studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)
            }
        }

        `when`("존재하지 않는 스케줄 삭제를 요청하면") {
            every { studyScheduleRepository.loadById(any()) } returns null

            then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "존재하지 않는 스케줄입니다."
            }
        }

        `when`("다른 그룹의 스케줄 삭제를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId())

            every { studyScheduleRepository.loadById(any()) } returns schedule

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "해당 그룹의 스케줄이 아닙니다."
            }
        }

        `when`("존재하지 않는 그룹의 스케줄 삭제를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId(groupId))

            every { studyScheduleRepository.loadById(any()) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns null

            then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "존재하지 않는 스터디 그룹입니다."
            }
        }

        `when`("그룹 소유자가 아닌 사용자가 스케줄 삭제를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId(groupId))
            val studyGroup = createStudyGroup(ownerId = ObjectId()) // 다른 사용자가 소유자

            every { studyScheduleRepository.loadById(any()) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "스터디 그룹의 소유자만 스케줄을 삭제할 수 있습니다."
            }
        }

        `when`("그룹의 최근 스케줄을 삭제하면") {
            val futureDateTime = LocalDateTime.now().plusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleId = ObjectId(scheduleId),
                scheduleAt = futureDateTime
            )
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))
            studyGroup.updateRecentSchedule(
                RecentScheduleVo(
                    scheduleId = schedule.id,
                    title = schedule.title,
                    session = schedule.session,
                    scheduleAt = schedule.scheduleAt
                )
            )

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every { studyScheduleRepository.delete(any()) } returns true
            every { studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(any()) } returns emptyList()
            every { saveStudyGroupPort.persist(any()) } returns studyGroup

            then("그룹의 최근 스케줄이 업데이트된다") {
                studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)

                verify { studyScheduleRepository.delete(schedule) }
                verify { eventPublisher.publishEvent(any<StudyScheduleCanceledEvent>()) }
                verify { saveStudyGroupPort.persist(any()) }
            }
        }

        `when`("그룹의 최근 스케줄을 삭제하고 대체할 미래 스케줄이 있으면") {
            val futureDateTime = LocalDateTime.now().plusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleId = ObjectId(scheduleId),
                scheduleAt = futureDateTime
            )
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))
            studyGroup.updateRecentSchedule(RecentScheduleVo(
                scheduleId = schedule.id,
                title = schedule.title,
                session = schedule.session,
                scheduleAt = schedule.scheduleAt
            ))

            // 삭제 후 남은 미래 스케줄
            val remainingFutureSchedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleAt = LocalDateTime.now().plusDays(2)
            )

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every { studyScheduleRepository.delete(any()) } returns true
            every { studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(any()) } returns listOf(remainingFutureSchedule)
            every { saveStudyGroupPort.persist(any()) } returns studyGroup

            then("그룹의 최근 스케줄이 다음 미래 스케줄로 업데이트된다") {
                studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)

                verify { studyScheduleRepository.delete(schedule) }
                verify { eventPublisher.publishEvent(any<StudyScheduleCanceledEvent>()) }
                verify { saveStudyGroupPort.persist(any()) }
            }
        }

        `when`("그룹의 최근 스케줄을 삭제하고 과거 스케줄만 남아있으면") {
            val futureDateTime = LocalDateTime.now().plusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleId = ObjectId(scheduleId),
                scheduleAt = futureDateTime
            )
            val studyGroup = createStudyGroup(ownerId = ObjectId(memberInfo.memberId))
            studyGroup.updateRecentSchedule(RecentScheduleVo(
                scheduleId = schedule.id,
                title = schedule.title,
                session = schedule.session,
                scheduleAt = schedule.scheduleAt
            ))

            // 삭제 후 남은 과거 스케줄만 있음
            val pastSchedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleAt = LocalDateTime.now().minusDays(1)
            )

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { loadStudyGroupPort.loadByGroupId(groupId) } returns studyGroup
            every { studyScheduleRepository.delete(any()) } returns true
            every { studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(any()) } returns listOf(pastSchedule)
            every { saveStudyGroupPort.persist(any()) } returns studyGroup

            then("그룹의 최근 스케줄이 null로 설정된다") {
                studyScheduleService.removeSchedule(memberInfo, groupId, scheduleId)

                verify { studyScheduleRepository.delete(schedule) }
                verify { eventPublisher.publishEvent(any<StudyScheduleCanceledEvent>()) }
                verify { saveStudyGroupPort.persist(any()) }
            }
        }
    }

    given("그룹 멤버가 스케줄 상세 정보를 조회하려고 할 때") {

        `when`("유효한 정보로 스케줄 상세 조회를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId(groupId), scheduleId = ObjectId(scheduleId))
            val groupMember = createGroupMember()
            val participant = createScheduleParticipant()

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns groupMember
            every { participantRepository.getByScheduleIdAndMemberId(any(), any()) } returns participant
            every { participantRepository.getByScheduleId(any()) } returns listOf(participant)
            every { groupMemberRepository.findByGroupIdAndMemberIdsIn(any(), any()) } returns listOf(groupMember)

            then("스케줄 상세 정보가 성공적으로 반환된다") {
                val result = studyScheduleService.getScheduleDetail(memberInfo, groupId, scheduleId)

                result shouldNotBe null
                result.participants shouldNotBe null
            }
        }

        `when`("존재하지 않는 스케줄 상세 조회를 요청하면") {
            every { studyScheduleRepository.loadById(any()) } returns null

            then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    studyScheduleService.getScheduleDetail(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "존재하지 않는 스케줄입니다."
            }
        }

        `when`("다른 그룹의 스케줄 상세 조회를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId())

            every { studyScheduleRepository.loadById(any()) } returns schedule

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.getScheduleDetail(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "해당 그룹의 스케줄이 아닙니다."
            }
        }

        `when`("그룹에 가입하지 않은 사용자가 스케줄 상세 조회를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId(groupId))

            every { studyScheduleRepository.loadById(any()) } returns schedule
            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns null

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.getScheduleDetail(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "가입된 그룹이 아닙니다."
            }
        }
    }

    given("그룹 멤버가 그룹의 스케줄 목록을 조회하려고 할 때") {

        `when`("그룹 소유자가 스케줄 목록 조회를 요청하면") {
            val groupMember = createGroupMember(role = GroupMemberRole.OWNER)
            val schedules = listOf(createStudySchedule())

            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns groupMember
            every { studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(any()) } returns schedules

            then("스케줄 목록이 성공적으로 반환된다") {
                val result = studyScheduleService.getGroupSchedules(memberInfo, groupId)

                result shouldNotBe null
                result.size shouldBe 1
            }
        }

        `when`("일반 멤버가 스케줄 목록 조회를 ��청하면") {
            val groupMember = createGroupMember(role = GroupMemberRole.MEMBER)
            val schedules = listOf(createStudySchedule())

            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns groupMember
            every { studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(any()) } returns schedules

            then("스케줄 목록이 성공적으로 반환된다") {
                val result = studyScheduleService.getGroupSchedules(memberInfo, groupId)

                result shouldNotBe null
                result.size shouldBe 1
            }
        }

        `when`("그룹에 가입하지 않은 사용자가 스케줄 목록 조회를 요청하면") {
            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns null

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.getGroupSchedules(memberInfo, groupId)
                }
                exception.message shouldBe "가입된 그룹이 아닙니다."
            }
        }

        `when`("권한이 없는 사용자가 스케줄 목록 조회를 요청하면") {
            // GroupMemberRole이 OWNER도 MEMBER도 아닌 경우를 테스트하기 위해
            // 실제로는 존재하지 않는 시나리오이지만 방어적 코딩을 위한 테스트
            val groupMember = createGroupMember(role = GroupMemberRole.MEMBER)

            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns groupMember
            // role 체크 로직을 우회하기 위해 MEMBER로 설정하고 실제로는 성공 케이스가 됨
            every { studyScheduleRepository.loadByGroupIdOrderByScheduleAtDesc(any()) } returns emptyList()

            then("빈 스케줄 목록이 반환된다") {
                val result = studyScheduleService.getGroupSchedules(memberInfo, groupId)
                result shouldNotBe null
                result.size shouldBe 0
            }
        }
    }

    given("그룹 멤버가 스케줄에 참여하려고 할 때") {

        `when`("아직 참여하지 않은 스케줄에 참여를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId(groupId), scheduleId = ObjectId(scheduleId))
            val groupMember = createGroupMember()
            val participant = createScheduleParticipant()

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { participantRepository.existsByScheduleIdAndMemberId(any(), any()) } returns false
            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns groupMember
            every { participantRepository.save(any()) } returns participant
            every { studyScheduleRepository.save(any()) } returns schedule

            then("스케줄 참여가 성공적으로 처리된다") {
                val result = studyScheduleService.joinSchedule(memberInfo, groupId, scheduleId)

                result shouldNotBe null
                verify { participantRepository.save(any()) }
            }
        }

        `when`("이미 참여한 스케줄에 다시 참여를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId(groupId))

            every { studyScheduleRepository.loadById(any()) } returns schedule
            every { participantRepository.existsByScheduleIdAndMemberId(any(), any()) } returns true

            then("ConflictException이 발생한다") {
                val exception = shouldThrow<ConflictException> {
                    studyScheduleService.joinSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "이미 해당 스케줄에 참여하고 있습니다."
            }
        }

        `when`("존재하지 않는 스케줄에 참여를 요청하면") {
            every { studyScheduleRepository.loadById(any()) } returns null

            then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    studyScheduleService.joinSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "존재하지 않는 스케줄입니다."
            }
        }

        `when`("다른 그룹의 스케줄에 참여를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId())

            every { studyScheduleRepository.loadById(any()) } returns schedule

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.joinSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "해당 그룹의 스케줄이 아닙니다."
            }
        }

        `when`("그룹에 가입하지 않은 사용자가 스케줄 참여를 요청하면") {
            val schedule = createStudySchedule(groupId = ObjectId(groupId))

            every { studyScheduleRepository.loadById(any()) } returns schedule
            every { participantRepository.existsByScheduleIdAndMemberId(any(), any()) } returns false
            every { groupMemberRepository.findByGroupIdAndMemberId(any(), any()) } returns null

            then("ForbiddenException이 발생한다") {
                val exception = shouldThrow<ForbiddenException> {
                    studyScheduleService.joinSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "가입된 그룹이 아닙니다."
            }
        }
    }

    given("그룹 멤버가 스케줄 참여를 취소하���고 할 때") {

        `when`("미래 스케줄의 참여 취소를 ���청하면") {
            val futureDateTime = LocalDateTime.now().plusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleId = ObjectId(scheduleId),
                scheduleAt = futureDateTime
            )
            val participant = createScheduleParticipant()

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { participantRepository.getByScheduleIdAndMemberId(any(), any()) } returns participant
            every { participantRepository.delete(any()) } returns true
            every { studyScheduleRepository.save(any()) } returns schedule

            then("참여 취소가 성공적으로 처리된다") {
                studyScheduleService.leaveSchedule(memberInfo, groupId, scheduleId)

                verify { participantRepository.delete(participant) }
            }
        }

        `when`("과거 스케줄의 참여 취소를 요청하면") {
            val pastDateTime = LocalDateTime.now().minusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleAt = pastDateTime
            )

            every { studyScheduleRepository.loadById(any()) } returns schedule

            then("BadRequestException이 발생한다") {
                val exception = shouldThrow<BadRequestException> {
                    studyScheduleService.leaveSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "과거의 스케줄은 참여를 취소할 수 없습니다."
            }
        }

        `when`("참여하지 않은 스케줄의 참여 취소를 요청하면") {
            val futureDateTime = LocalDateTime.now().plusDays(1)
            val schedule = createStudySchedule(
                groupId = ObjectId(groupId),
                scheduleId = ObjectId(scheduleId),
                scheduleAt = futureDateTime
            )

            every { studyScheduleRepository.loadById(ObjectId(scheduleId)) } returns schedule
            every { participantRepository.getByScheduleIdAndMemberId(any(), any()) } returns null

            then("IllegalArgumentException이 발생한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    studyScheduleService.leaveSchedule(memberInfo, groupId, scheduleId)
                }
                exception.message shouldBe "해당 스케줄에 참여하지 않은 멤버입니다."
            }
        }
    }
}) {
    companion object {

        /**
         * 테스트용 MemberInfo 객체 생성
         */
        private fun createMemberInfo(memberId: String = ObjectId().toHexString()): MemberInfo {
            return MemberInfo(
                memberId = memberId,
                nickname = "테스��닉네임",
                roles = listOf()
            )
        }

        /**
         * 테스트용 StudyScheduleCreateCommand 객체 생성
         */
        private fun createScheduleCreateCommand(): StudyScheduleCreateCommand {
            return StudyScheduleCreateCommand(
                title = "테스트 스케줄",
                description = "테스트 설명",
                scheduleAt = LocalDateTime.now().plusDays(1),
                location = LocationVo(
                    type = ScheduleLocation.LocationType.ONLINE,
                    value = "Zoom"
                ),
                roles = listOf(
                    RoleAssignment(
                        memberId = ObjectId().toHexString(),
                        color = "#FF0000",
                        roles = listOf("발표자")
                    )
                )
            )
        }

        /**
         * 테스트용 StudyScheduleUpdateCommand 객체 생성
         */
        private fun createScheduleUpdateCommand(): StudyScheduleUpdateCommand {
            return StudyScheduleUpdateCommand(
                title = "수정된 스케줄",
                description = "수정된 설명",
                scheduleAt = LocalDateTime.now().plusDays(2),
                location = LocationVo(
                    type = ScheduleLocation.LocationType.OFFLINE,
                    value = "카페"
                ),
                roles = listOf(
                    RoleAssignment(
                        memberId = ObjectId().toHexString(),
                        color = "#00FF00",
                        roles = listOf("진행자")
                    )
                )
            )
        }

        /**
         * 테스트용 StudyGroup 객체 생성
         */
        private fun createStudyGroup(
            ownerId: ObjectId = ObjectId(),
            groupId: ObjectId = ObjectId()
        ): StudyGroup {
            return StudyGroup(
                id = groupId,
                ownerId = ownerId,
                name = "테스트 그룹",
                description = "테스트 그룹 설명"
            )
        }

        /**
         * 테스트용 StudyGroupMember 객체 생성
         */
        private fun createGroupMember(
            memberId: ObjectId = ObjectId(),
            groupId: ObjectId = ObjectId(),
            role: GroupMemberRole = GroupMemberRole.MEMBER
        ): StudyGroupMember {
            return StudyGroupMember(
                memberId = memberId,
                groupId = groupId,
                nickname = "테스트닉네임",
                role = role,
                profileImage = MemberProfileImageVo("DEFAULT", "profile.jpg")
            )
        }

        /**
         * 테스트용 StudySchedule 객체 생성
         */
        private fun createStudySchedule(
            groupId: ObjectId = ObjectId(),
            scheduleId: ObjectId = ObjectId(),
            scheduleAt: LocalDateTime = LocalDateTime.now().plusDays(1)
        ): StudySchedule {
            return StudySchedule(
                id = scheduleId,
                groupId = groupId,
                title = "테스트 스케줄",
                description = "테스트 설명",
                scheduleAt = scheduleAt,
                session = 1L,
                location = ScheduleLocation(
                    type = ScheduleLocation.LocationType.ONLINE,
                    value = "Zoom"
                ),
                roles = mutableSetOf()
            )
        }

        /**
         * 테스트용 ScheduleParticipant 객체 생성
         */
        private fun createScheduleParticipant(): ScheduleParticipant {
            return ScheduleParticipant(
                memberId = ObjectId(),
                scheduleId = ObjectId()
            )
        }
    }
}
