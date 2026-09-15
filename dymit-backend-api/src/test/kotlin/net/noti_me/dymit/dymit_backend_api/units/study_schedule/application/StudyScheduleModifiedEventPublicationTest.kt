package net.noti_me.dymit.dymit_backend_api.units.study_schedule.application

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.member.domain.MemberRole
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.StudyScheduleServiceImpl
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleModifiedEventDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.LocationVo
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.ScheduleParticipantRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.persistence.StudyScheduleRepository
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.StudyScheduleGroupPort
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberRoleDto
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.usecase.dto.StudyScheduleUpdateCommand
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleLocation
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant

internal class StudyScheduleModifiedEventPublicationTest : BehaviorSpec() {

    private val groupPort = mockk<StudyScheduleGroupPort>()
    private val scheduleRepository = mockk<StudyScheduleRepository>()
    private val participantRepository = mockk<ScheduleParticipantRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val service = StudyScheduleServiceImpl(
        groupPort = groupPort,
        studyScheduleRepository = scheduleRepository,
        participantRepository = participantRepository,
        eventPublisher = eventPublisher
    )

    init {
        afterEach {
            clearAllMocks()
        }

        Given("스터디 일정 시작 시각을 변경하면") {
            Then("새 시작 시각을 포함한 일정 수정 이벤트를 발행한다") {
                val groupId = ObjectId.get()
                val ownerId = ObjectId.get()
                val scheduleId = ObjectId.get()
                val originalScheduleAt = Instant.parse("2026-09-20T00:00:00Z")
                val updatedScheduleAt = Instant.parse("2026-09-21T00:00:00Z")
                val group = StudyScheduleGroupDto(
                    id = groupId,
                    ownerId = ownerId,
                    name = "스터디"
                )
                val owner = StudyScheduleGroupMemberDto(
                    groupId = groupId,
                    memberId = ownerId,
                    nickname = "모임장",
                    role = StudyScheduleGroupMemberRoleDto.OWNER
                )
                val schedule = StudySchedule(
                    id = scheduleId,
                    groupId = groupId,
                    title = "주간 모임",
                    description = "주간 모임 설명",
                    location = ScheduleLocation(
                        type = ScheduleLocation.LocationType.ONLINE,
                        value = "회의 링크",
                        link = "https://example.com"
                    ),
                    scheduleAt = originalScheduleAt
                )
                val memberInfo = MemberInfo(
                    memberId = ownerId.toHexString(),
                    nickname = owner.nickname,
                    roles = listOf(MemberRole.ROLE_MEMBER.name)
                )
                val eventSlot = slot<Any>()

                every { scheduleRepository.loadById(scheduleId) } returns schedule
                every { groupPort.loadByGroupId(groupId.toHexString()) } returns group
                every { groupPort.findMember(groupId, ownerId) } returns owner
                every { groupPort.findMembers(groupId, emptyList()) } returns emptyList()
                every { scheduleRepository.save(schedule) } returns schedule
                every { groupPort.persist(group) } returns group
                every { participantRepository.getByScheduleId(scheduleId) } returns emptyList()
                justRun { eventPublisher.publishEvent(any()) }

                service.updateSchedule(
                    memberInfo = memberInfo,
                    groupId = groupId.toHexString(),
                    scheduleId = scheduleId.toHexString(),
                    command = StudyScheduleUpdateCommand(
                        title = schedule.title,
                        description = schedule.description,
                        scheduleAt = updatedScheduleAt,
                        location = LocationVo.from(schedule.location),
                        roles = emptyList()
                    )
                )

                verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                val event = eventSlot.captured as StudyScheduleModifiedEventDto
                event.schedule.id shouldBe scheduleId.toHexString()
                event.scheduleAt shouldBe updatedScheduleAt
                event.memberIds shouldBe emptyList()
            }
        }
    }
}
