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
