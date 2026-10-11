package net.noti_me.dymit.dymit_backend_api.units.study_group.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.supports.MemberStatisticsRankingFixture
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.study_group.application.StatisticsQueryService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.GetGroupMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.MemberStatisticsDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.RefreshMemberStatisticsCommand
import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StatisticsCounts
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import org.bson.types.ObjectId

internal class StatisticsQueryPaginationTest : BehaviorSpec({

    listOf(-1, 0, 101).forEach { size ->
        Given("허용 범위를 벗어난 size $size") {
            Then("저장소 조회와 통계 갱신 전에 잘못된 요청을 거부한다") {
                val members = mockk<StudyGroupMemberRepository>()
                val repository = mockk<StatisticsRepository>()
                val refresh = mockk<RefreshMemberStatisticsUseCase>()
                val schedules = mockk<ScheduleStatisticsSourcePort>()
                every { schedules.loadBoundaries(any(), any()) } returns emptyMap()
                shouldThrow<BadRequestException> {
                    StatisticsQueryService(members, mockk(), repository, schedules, refresh).execute(
                        GetGroupMemberStatisticsCommand(ObjectId().toHexString(), ObjectId().toHexString(), size = size)
                    )
                }
                verify { members wasNot Called }
                verify { refresh wasNot Called }
            }
        }
    }

    listOf(1, 20, 100).forEach { size ->
        Given("관리자가 size $size 로 구성원 통계 목록을 요청") {
            Then("활성 구성원 전체를 갱신한 뒤 외부 커서 다음부터 size+1개를 반환한다") {
                val f = MemberStatisticsRankingFixture()
                val entries = List(size + 2) { f.addMember() }
                val cursor = if (size == 20) null else entries.first().identifier
                val result = f.execute(cursor, size)
                val start = if (cursor == null) 0 else 1
                result.map { it.membershipId } shouldBe entries.drop(start).take(size + 1).map { it.identifier }
                f.commands.map { it.membershipId } shouldBe entries.map { it.identifier }
                f.commands.map { it.boundaries }.distinct() shouldBe listOf(f.boundaries)
                f.commands.map { it.observedAt }.distinct().size shouldBe 1
                f.internalQueries shouldBe listOf(null to 200)
                verify(exactly = 0) { f.members.findByGroupIdIncludingDeleted(any(), any(), any()) }
            }
        }
    }

    Given("그룹에 속하지 않은 요청자") {
        Then("목록 조회와 갱신 전에 접근을 거부한다") {
            val members = mockk<StudyGroupMemberRepository>()
            val refresh = mockk<RefreshMemberStatisticsUseCase>()
                val schedules = mockk<ScheduleStatisticsSourcePort>()
                every { schedules.loadBoundaries(any(), any()) } returns emptyMap()
            every { members.findByGroupIdAndMemberId(any(), any()) } returns null
            shouldThrow<NotFoundException> {
                StatisticsQueryService(members, mockk(), mockk(), schedules, refresh).execute(
                    GetGroupMemberStatisticsCommand(ObjectId().toHexString(), ObjectId().toHexString())
                )
            }
            verify(exactly = 0) { members.findActiveByGroupId(any(), any(), any()) }
            verify { refresh wasNot Called }
        }
    }
})
