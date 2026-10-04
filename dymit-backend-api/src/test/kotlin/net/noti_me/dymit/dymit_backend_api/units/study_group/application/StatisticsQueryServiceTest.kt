package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.study_group.application.StatisticsQueryService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.*
import net.noti_me.dymit.dymit_backend_api.study_group.domain.*
import org.bson.types.ObjectId
import java.time.Instant

internal class StatisticsQueryServiceTest : BehaviorSpec({
    Given("저장된 그룹 집계가 오래됐고 탈퇴 및 재가입 구간이 있는 그룹") {
        Then("모든 가입 구간을 공통 갱신하고 저장된 두 주 지표를 최신 건수 합계로 교체한다") {
            val members = mockk<StudyGroupMemberRepository>()
            val repository = mockk<StatisticsRepository>()
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
            val commands = mutableListOf<RefreshMemberStatisticsCommand>()
            every { members.findByGroupIdAndMemberId(group, requester) } returns manager
            every { members.findByGroupIdIncludingDeleted(group, null, any()) } returns listOf(manager, departed, rejoined)
            every { repository.findLedger(any()) } returns null
            every { refresh.execute(any()) } answers {
                val command = firstArg<RefreshMemberStatisticsCommand>()
                commands.add(command)
                MemberStatisticsDto(group.toHexString(), command.membershipId, requester.toHexString(),
                    command.cutoff, StatisticsCounts(), 0.0, 0.0)
            }
            every { repository.findLatestGroupWeek(group, any()) } answers {
                GroupWeeklyStatistics(groupId = group, weekEnd = secondArg(), counts = StatisticsCounts(99, 99),
                    version = 1, updatedAt = Instant.EPOCH)
            }
            every { repository.findGroupWeek(group, any()) } answers {
                GroupWeeklyStatistics(groupId = group, weekEnd = secondArg(), counts = StatisticsCounts(99, 99),
                    version = 1, updatedAt = Instant.EPOCH)
            }
            every { repository.sumLatestMemberCounts(group, any()) } answers {
                if (secondArg<Instant>() == commands.first().cutoff) StatisticsCounts(2, 10, 2, 10)
                else StatisticsCounts(1, 10, 1, 10)
            }
            every { repository.compareAndSetGroupWeek(any(), any()) } returns true
            val result = StatisticsQueryService(members, repository, refresh).execute(
                GetGroupStatisticsCommand(requester.toHexString(), group.toHexString())
            )
            commands.map { it.membershipId }.toSet() shouldBe setOf(manager.identifier, departed.identifier, rejoined.identifier)
            commands.map { it.cutoff }.distinct().size shouldBe 1
            commands.map { it.observedAt }.distinct().size shouldBe 1
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
            val repository = mockk<StatisticsRepository>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
            val group = ObjectId()
            val requester = ObjectId()
            val other = ObjectId()
            every { members.findByGroupIdAndMemberId(group, requester) } returns StudyGroupMember(memberId = requester)
            every { members.findByGroupIdAndMemberId(group, other) } returns StudyGroupMember(memberId = other)
            shouldThrow<ForbiddenException> {
                StatisticsQueryService(members, repository, refresh).execute(
                    GetMemberStatisticsCommand(requester.toHexString(), group.toHexString(), other.toHexString())
                )
            }
            shouldThrow<ForbiddenException> {
                StatisticsQueryService(members, repository, refresh).execute(
                    GetGroupStatisticsCommand(requester.toHexString(), group.toHexString())
                )
            }
            shouldThrow<ForbiddenException> {
                StatisticsQueryService(members, repository, refresh).execute(
                    GetGroupMemberStatisticsCommand(requester.toHexString(), group.toHexString(), null, 10)
                )
            }
            verify(exactly = 0) { refresh.execute(any()) }
        }
    }

    Given("구성원 본인이 개인 통계를 조회") {
        Then("그룹 집계와 동일한 사용자 원장 갱신 경로를 사용한다") {
            val members = mockk<StudyGroupMemberRepository>()
            val repository = mockk<StatisticsRepository>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
            val group = ObjectId()
            val member = ObjectId()
            val membership = StudyGroupMember(id = ObjectId(), groupId = group, memberId = member)
            every { members.findByGroupIdAndMemberId(group, member) } returns membership
            every { refresh.execute(any()) } answers {
                val command = firstArg<RefreshMemberStatisticsCommand>()
                MemberStatisticsDto(group.toHexString(), membership.identifier, member.toHexString(), command.cutoff,
                    StatisticsCounts(1, 2), 50.0, 0.0)
            }
            StatisticsQueryService(members, repository, refresh).execute(
                GetMemberStatisticsCommand(member.toHexString(), group.toHexString(), member.toHexString())
            ).counts shouldBe StatisticsCounts(1, 2)
            verify(exactly = 1) { refresh.execute(match { it.membershipId == membership.identifier }) }
        }
    }
})
