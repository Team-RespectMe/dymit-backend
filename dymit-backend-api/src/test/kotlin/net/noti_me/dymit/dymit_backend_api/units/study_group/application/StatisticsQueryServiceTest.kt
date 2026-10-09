package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.study_group.application.StatisticsQueryService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSessionBoundary
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.*
import net.noti_me.dymit.dymit_backend_api.study_group.domain.*
import org.bson.types.ObjectId
import java.time.Instant

internal class StatisticsQueryServiceTest : BehaviorSpec({
    Given("오래된 그룹 집계와 탈퇴·재가입 이력 및 불규칙한 실제 회차") {
        Then("모든 가입 구간과 누락 회차를 갱신하고 실제 최신·직전 회차 및 활성 인원을 반환한다") {
            val members = mockk<StudyGroupMemberRepository>()
            val groups = mockk<LoadStudyGroupPort>()
            val repository = mockk<StatisticsRepository>()
            val schedules = mockk<ScheduleStatisticsSourcePort>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
            val group = ObjectId()
            val requester = ObjectId()
            val manager = StudyGroupMember(id = ObjectId(), groupId = group, memberId = requester,
                role = GroupMemberRole.OWNER, createdAt = Instant.parse("2026-01-01T00:00:00Z"))
            val departed = StudyGroupMember(id = ObjectId(), groupId = group, memberId = ObjectId(),
                createdAt = Instant.parse("2026-01-01T00:00:00Z"), isDeleted = true,
                deletedAt = Instant.parse("2026-01-02T00:00:00Z"))
            val rejoined = StudyGroupMember(id = ObjectId(), groupId = group, memberId = departed.memberId,
                createdAt = Instant.parse("2026-01-03T00:00:00Z"))
            val boundaries = listOf(
                StatisticsSessionBoundary(group, ObjectId(), 3, Instant.parse("2026-09-03T11:00:00Z")),
                StatisticsSessionBoundary(group, ObjectId(), 8, Instant.parse("2026-09-03T13:00:00Z")),
                StatisticsSessionBoundary(group, ObjectId(), 12, Instant.parse("2026-09-18T11:00:00Z"))
            )
            val commands = mutableListOf<RefreshMemberStatisticsCommand>()
            val saved = mutableListOf<GroupSessionStatistics>()
            every { members.findByGroupIdAndMemberId(group, requester) } returns manager
            every { members.findByGroupIdIncludingDeleted(group, null, any()) } returns listOf(manager, departed, rejoined)
            every { groups.loadByGroupId(group.toHexString()) } returns StudyGroup(id = group, name = "불규칙 회차 그룹")
            every { members.countDistinctActiveMembers(listOf(group)) } returns mapOf(group to 2L)
            every { schedules.loadBoundaries(listOf(group), any()) } returns mapOf(group to boundaries.reversed())
            every { refresh.execute(capture(commands)) } answers {
                val command = firstArg<RefreshMemberStatisticsCommand>()
                MemberStatisticsDto(group.toHexString(), command.membershipId, requester.toHexString(),
                    12, command.boundaries.last().scheduleAt, StatisticsCounts(), 0.0, 0.0)
            }
            every { repository.findLedgersByGroupId(group) } returns emptyList()
            every { repository.findGroupSessions(group) } returns boundaries.map { boundary ->
                GroupSessionStatistics(groupId = group, scheduleId = boundary.scheduleId, session = boundary.session,
                    scheduleAt = boundary.scheduleAt, statisticsAt = boundary.scheduleAt,
                    counts = StatisticsCounts(99, 99), sourceProjectionToken = "old", version = 1, updatedAt = Instant.EPOCH)
            }
            every { repository.findGroupSession(group, any()) } answers {
                val boundary = boundaries.single { it.scheduleId == secondArg<ObjectId>() }
                GroupSessionStatistics(groupId = group, scheduleId = boundary.scheduleId, session = boundary.session,
                    scheduleAt = boundary.scheduleAt, statisticsAt = boundary.scheduleAt,
                    counts = StatisticsCounts(99, 99), sourceProjectionToken = "old", version = 1, updatedAt = Instant.EPOCH)
            }
            every { repository.sumMemberCounts(group, any(), any()) } answers {
                if (secondArg<ObjectId>() == boundaries.last().scheduleId) StatisticsCounts(2, 10, 2, 10)
                else StatisticsCounts(1, 10, 1, 10)
            }
            every { repository.compareAndSetGroupSession(any(), capture(saved)) } returns true
            val result = StatisticsQueryService(members, groups, repository, schedules, refresh).execute(
                GetGroupStatisticsCommand(requester.toHexString(), group.toHexString())
            )
            commands.map { it.membershipId }.toSet() shouldBe setOf(manager.identifier, departed.identifier, rejoined.identifier)
            commands.map { it.boundaries }.distinct() shouldBe listOf(boundaries)
            commands.map { it.observedAt }.distinct().size shouldBe 1
            saved.map { it.scheduleId }.toSet() shouldBe boundaries.map { it.scheduleId }.toSet()
            result.latestSession shouldBe 12
            result.previousSession shouldBe 8
            result.statisticsAt shouldBe boundaries.last().scheduleAt
            result.activeMemberCount shouldBe 2
            result.counts shouldBe StatisticsCounts(2, 10, 2, 10)
            result.taskSubmissionRate shouldBe 20.0
            result.previousTaskSubmissionRate shouldBe 10.0
            result.scheduleAttendanceRate shouldBe 20.0
            result.previousScheduleAttendanceRate shouldBe 10.0
        }
    }

    Given("일반 구성원이 다른 구성원의 통계를 요청") {
        Then("원장 갱신 전에 접근을 거부한다") {
            val members = mockk<StudyGroupMemberRepository>()
            val groups = mockk<LoadStudyGroupPort>()
            val repository = mockk<StatisticsRepository>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
            val schedules = mockk<ScheduleStatisticsSourcePort>()
            every { schedules.loadBoundaries(any(), any()) } returns emptyMap()
            val group = ObjectId()
            val requester = ObjectId()
            val other = ObjectId()
            every { members.findByGroupIdAndMemberId(group, requester) } returns StudyGroupMember(memberId = requester)
            every { members.findByGroupIdAndMemberId(group, other) } returns StudyGroupMember(memberId = other)
            shouldThrow<ForbiddenException> {
                StatisticsQueryService(members, groups, repository, schedules, refresh).execute(
                    GetMemberStatisticsCommand(requester.toHexString(), group.toHexString(), other.toHexString())
                )
            }
            shouldThrow<ForbiddenException> {
                StatisticsQueryService(members, groups, repository, schedules, refresh).execute(
                    GetGroupStatisticsCommand(requester.toHexString(), group.toHexString())
                )
            }
            shouldThrow<ForbiddenException> {
                StatisticsQueryService(members, groups, repository, schedules, refresh).execute(
                    GetGroupMemberStatisticsCommand(requester.toHexString(), group.toHexString(), null, 10)
                )
            }
            verify(exactly = 0) { refresh.execute(any()) }
            verify { groups wasNot Called }
        }
    }

    Given("구성원 본인이 개인 통계를 조회") {
        Then("그룹 집계와 동일한 사용자 원장 갱신 경로를 사용한다") {
            val members = mockk<StudyGroupMemberRepository>()
            val groups = mockk<LoadStudyGroupPort>()
            val repository = mockk<StatisticsRepository>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
            val schedules = mockk<ScheduleStatisticsSourcePort>()
            every { schedules.loadBoundaries(any(), any()) } returns emptyMap()
            val group = ObjectId()
            val member = ObjectId()
            val membership = StudyGroupMember(id = ObjectId(), groupId = group, memberId = member)
            every { members.findByGroupIdAndMemberId(group, member) } returns membership
            every { refresh.execute(any()) } answers {
                MemberStatisticsDto(group.toHexString(), membership.identifier, member.toHexString(), null, null,
                    StatisticsCounts(1, 2), 50.0, 0.0)
            }
            StatisticsQueryService(members, groups, repository, schedules, refresh).execute(
                GetMemberStatisticsCommand(member.toHexString(), group.toHexString(), member.toHexString())
            ).counts shouldBe StatisticsCounts(1, 2)
            verify(exactly = 1) { refresh.execute(match { it.membershipId == membership.identifier }) }
        }
    }

    Given("아직 진행 회차가 없는 그룹 상세 조회") {
        Then("최근·이전 회차와 기준 시각은 null이고 현재 인원 및 네 비율 0을 반환한다") {
            val group = ObjectId()
            val requester = ObjectId()
            val members = mockk<StudyGroupMemberRepository>()
            val groups = mockk<LoadStudyGroupPort>()
            val repository = mockk<StatisticsRepository>()
            val schedules = mockk<ScheduleStatisticsSourcePort>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
            every { members.findByGroupIdAndMemberId(group, requester) } returns StudyGroupMember(
                groupId = group, memberId = requester, role = GroupMemberRole.OWNER
            )
            every { groups.loadByGroupId(group.toHexString()) } returns StudyGroup(id = group, name = "미래 일정 그룹")
            every { members.countDistinctActiveMembers(listOf(group)) } returns mapOf(group to 4L)
            every { schedules.loadBoundaries(listOf(group), any()) } returns emptyMap()
            val result = StatisticsQueryService(members, groups, repository, schedules, refresh).execute(
                GetGroupStatisticsCommand(requester.toHexString(), group.toHexString())
            )
            result.latestSession shouldBe null
            result.previousSession shouldBe null
            result.statisticsAt shouldBe null
            result.activeMemberCount shouldBe 4
            result.taskSubmissionRate shouldBe 0.0
            result.scheduleAttendanceRate shouldBe 0.0
            result.previousTaskSubmissionRate shouldBe 0.0
            result.previousScheduleAttendanceRate shouldBe 0.0
            verify(exactly = 0) { refresh.execute(any()) }
            verify(exactly = 0) { members.findByGroupIdIncludingDeleted(any(), any(), any()) }
        }
    }
})
