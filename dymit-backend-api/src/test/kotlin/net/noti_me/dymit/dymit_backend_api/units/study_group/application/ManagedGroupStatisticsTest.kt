package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_group.application.StatisticsQueryService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.*
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.*
import net.noti_me.dymit.dymit_backend_api.study_group.domain.*
import org.bson.types.ObjectId
import java.time.Instant

internal class ManagedGroupStatisticsTest : BehaviorSpec({

    Given("과거 진행 그룹과 미래 일정만 있는 그룹을 관리하는 사용자") {
        Then("실제 반환 그룹만 갱신하며 경계·활성 인원·일정 존재를 배치 조회하고 다음 후보는 갱신하지 않는다") {
            val members = mockk<StudyGroupMemberRepository>()
            val repository = mockk<StatisticsRepository>()
            val schedules = mockk<ScheduleStatisticsSourcePort>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
            val requester = ObjectId()
            val groups = List(3) { ObjectId() }.sorted()
            val pageIds = groups.take(2)
            val groupPort = mockk<LoadStudyGroupPort>()
            var names = groups.indices.map { "그룹 $it" }
            every { groupPort.loadByGroupIds(groups.map { it.toHexString() }) } answers {
                groups.mapIndexed { index, id -> StudyGroup(id = id, name = names[index]) }
            }
            val cursor = ObjectId()
            val boundary = StatisticsSessionBoundary(groups[0], ObjectId(), 9, Instant.parse("2026-09-22T13:00:00Z"))
            val membership = StudyGroupMember(id = ObjectId(), groupId = groups[0], memberId = requester,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"), role = GroupMemberRole.ADMIN)
            val commands = mutableListOf<RefreshMemberStatisticsCommand>()
            every { members.findManagedGroupIds(requester, cursor, 3) } returns groups
            every { members.countDistinctActiveMembers(pageIds) } returnsMany listOf(
                mapOf(groups[0] to 3L, groups[1] to 2L), mapOf(groups[0] to 4L, groups[1] to 1L)
            )
            every { schedules.loadBoundaries(pageIds, any()) } returns mapOf(groups[0] to listOf(boundary))
            every { schedules.loadGroupIdsHavingSchedule(pageIds) } returns pageIds.toSet()
            every { members.findByGroupIdIncludingDeleted(groups[0], null, any()) } returns listOf(membership)
            every { refresh.execute(capture(commands)) } returns MemberStatisticsDto(
                groups[0].toHexString(), membership.identifier, requester.toHexString(), 9, boundary.scheduleAt,
                StatisticsCounts(2, 4, 1, 4), 50.0, 25.0
            )
            every { repository.findGroupSession(groups[0], boundary.scheduleId) } returns null
            every { repository.findGroupSessions(groups[0]) } returns emptyList()
            every { repository.findLedgersByGroupId(groups[0]) } returns emptyList()
            every { repository.sumMemberCounts(groups[0], boundary.scheduleId, any()) } returns StatisticsCounts(2, 4, 1, 4)
            every { repository.compareAndSetGroupSession(any(), any()) } returns true
            val service = StatisticsQueryService(members, groupPort, repository, schedules, refresh)
            val command = GetManagedGroupStatisticsCommand(requester.toHexString(), cursor.toHexString(), 2)
            val first = service.execute(command)
            first.size shouldBe 3
            first[0].latestSession shouldBe 9
            first[0].activeMemberCount shouldBe 3
            first[0].taskSubmissionRate shouldBe 50.0
            first[0].scheduleAttendanceRate shouldBe 25.0
            first[1].latestSession shouldBe null
            first[1].statisticsAt shouldBe null
            first[1].activeMemberCount shouldBe 2
            first[1].taskSubmissionRate shouldBe 0.0
            first[1].hasSchedule shouldBe true
            first[2].group.id shouldBe groups[2].toHexString()
            first.map { it.group.name } shouldBe names
            names = groups.indices.map { "변경된 그룹 $it" }
            val second = service.execute(command)
            second.map { it.group.name } shouldBe names
            second.take(2).map { it.activeMemberCount } shouldBe listOf(4L, 1L)
            commands.all { it.boundaries == listOf(boundary) } shouldBe true
            verify(exactly = 2) { groupPort.loadByGroupIds(groups.map { it.toHexString() }) }
            verify(exactly = 0) { groupPort.loadByGroupId(any()) }
            verify(exactly = 2) { schedules.loadBoundaries(pageIds, any()) }
            verify(exactly = 2) { schedules.loadGroupIdsHavingSchedule(pageIds) }
            verify(exactly = 2) { members.countDistinctActiveMembers(pageIds) }
            verify(exactly = 0) { members.findByGroupIdIncludingDeleted(groups[1], any(), any()) }
            verify(exactly = 0) { members.findByGroupIdIncludingDeleted(groups[2], any(), any()) }
            verify(exactly = 0) { members.findByGroupIdAndMemberId(any(), any()) }
        }
    }

    listOf(0, 101).forEach { size ->
        Given("관리 목록 size $size 요청") {
            Then("후보 목록이나 외부 원천 조회 전에 잘못된 크기를 거부한다") {
                val members = mockk<StudyGroupMemberRepository>()
                val schedules = mockk<ScheduleStatisticsSourcePort>()
                shouldThrow<BadRequestException> {
                    StatisticsQueryService(members, mockk(), mockk(), schedules, mockk()).execute(
                        GetManagedGroupStatisticsCommand(ObjectId().toHexString(), size = size)
                    )
                }
                verify { members wasNot Called }
                verify { schedules wasNot Called }
            }
        }
    }

    listOf("누락", "삭제").forEach { state ->
        Given("관리 목록 후보 그룹의 현재 정보가 $state 상태") {
            Then("불완전한 이름을 반환하지 않고 배치 조회 직후 존재하지 않는 그룹으로 처리한다") {
                val members = mockk<StudyGroupMemberRepository>()
                val groups = mockk<LoadStudyGroupPort>()
                val schedules = mockk<ScheduleStatisticsSourcePort>()
                val requester = ObjectId()
                val group = ObjectId()
                every { members.findManagedGroupIds(requester, null, 2) } returns listOf(group)
                every { groups.loadByGroupIds(listOf(group.toHexString())) } returns
                    if (state == "누락") emptyList() else listOf(StudyGroup(id = group, name = "삭제 그룹", isDeleted = true))
                shouldThrow<NotFoundException> {
                    StatisticsQueryService(members, groups, mockk(), schedules, mockk()).execute(
                        GetManagedGroupStatisticsCommand(requester.toHexString(), size = 1)
                    )
                }
                verify(exactly = 1) { groups.loadByGroupIds(listOf(group.toHexString())) }
                verify(exactly = 0) { groups.loadByGroupId(any()) }
                verify { schedules wasNot Called }
            }
        }
    }
})
