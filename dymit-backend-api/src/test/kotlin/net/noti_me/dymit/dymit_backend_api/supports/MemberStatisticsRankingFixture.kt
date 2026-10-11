package net.noti_me.dymit.dymit_backend_api.supports

import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.study_group.application.StatisticsQueryService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.ScheduleStatisticsSourcePort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.StatisticsSessionBoundary
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.*
import net.noti_me.dymit.dymit_backend_api.study_group.domain.*
import org.bson.types.ObjectId
import java.time.Instant

/** 활성 구성원 배치와 갱신 결과를 격리한 순위 목록 테스트 데이터입니다. */
internal class MemberStatisticsRankingFixture {

    val group = ObjectId()
    val requester = ObjectId()
    val members = mockk<StudyGroupMemberRepository>()
    val schedules = mockk<ScheduleStatisticsSourcePort>()
    val refresh = mockk<RefreshMemberStatisticsUseCase>()
    var activeMembers = emptyList<StudyGroupMember>()
    val rates = mutableMapOf<String, Pair<Double, Double>>()
    val commands = mutableListOf<RefreshMemberStatisticsCommand>()
    val internalQueries = mutableListOf<Pair<ObjectId?, Int>>()
    var boundaries = listOf(StatisticsSessionBoundary(group, ObjectId(), 9, Instant.parse("2026-10-01T09:00:00Z")))
    val service = StatisticsQueryService(members, mockk(), mockk(), schedules, refresh)

    init {
        every { members.findByGroupIdAndMemberId(group, requester) } returns StudyGroupMember(
            id = ObjectId(), groupId = group, memberId = requester, role = GroupMemberRole.OWNER
        )
        every { members.findActiveByGroupId(group, any(), any()) } answers {
            val cursor = secondArg<ObjectId?>()
            val limit = thirdArg<Int>()
            internalQueries.add(cursor to limit)
            val start = if (cursor == null) 0 else activeMembers.indexOfFirst { it.id == cursor } + 1
            activeMembers.drop(start).take(limit)
        }
        every { schedules.loadBoundaries(listOf(group), any()) } answers { mapOf(group to boundaries) }
        every { refresh.execute(capture(commands)) } answers {
            val command = firstArg<RefreshMemberStatisticsCommand>()
            val membership = activeMembers.single { it.identifier == command.membershipId }
            val (taskRate, attendanceRate) = rates.getValue(command.membershipId)
            MemberStatisticsDto(group.toHexString(), membership.identifier, membership.memberId.toHexString(),
                command.boundaries.lastOrNull()?.session, command.boundaries.lastOrNull()?.scheduleAt,
                StatisticsCounts(), taskRate, attendanceRate)
        }
    }

    fun addMember(
        taskRate: Double = 0.0,
        attendanceRate: Double = 0.0,
        id: ObjectId = ObjectId()
    ): StudyGroupMember {
        val membership = StudyGroupMember(id = id, groupId = group, memberId = ObjectId(), createdAt = Instant.EPOCH)
        activeMembers = activeMembers + membership
        rates[membership.identifier] = taskRate to attendanceRate
        return membership
    }

    fun execute(cursor: String? = null, size: Int = 20): List<MemberStatisticsDto> = service.execute(
        GetGroupMemberStatisticsCommand(requester.toHexString(), group.toHexString(), cursor, size)
    )
}
