package net.noti_me.dymit.dymit_backend_api.supports

import io.mockk.every
import io.mockk.mockk
import net.noti_me.dymit.dymit_backend_api.study_group.application.StatisticsQueryService
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.statistics.*
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.RefreshMemberStatisticsUseCase
import net.noti_me.dymit.dymit_backend_api.study_group.application.usecase.dto.*
import net.noti_me.dymit.dymit_backend_api.study_group.domain.*
import org.bson.types.ObjectId
import java.time.Instant

/** 그룹 회차별 고정 회원 집합과 서로 다른 개인 기여를 검증합니다. */
internal class FrozenRosterStatisticsFixture {

    val group = ObjectId()
    val first = StatisticsSessionBoundary(group, ObjectId(), 1, Instant.parse("2026-09-01T10:00:00Z"))
    val second = StatisticsSessionBoundary(group, ObjectId(), 2, Instant.parse("2026-09-08T10:00:00Z"))
    val manager = member(GroupMemberRole.OWNER)
    val other = member()
    var all = listOf(manager, other)
    var active = all
    var boundaries = listOf(first, second)
    val members = mockk<StudyGroupMemberRepository>()
    val groups = mockk<LoadStudyGroupPort>()
    val repository = mockk<StatisticsRepository>()
    val schedules = mockk<ScheduleStatisticsSourcePort>()
    val refresh = mockk<RefreshMemberStatisticsUseCase>()
    val stored = mutableMapOf<ObjectId, GroupSessionStatistics>()
    val ledgers = mutableMapOf<ObjectId, MemberSessionStatisticsLedger>()
    val commands = mutableListOf<RefreshMemberStatisticsCommand>()
    val saved = mutableListOf<GroupSessionStatistics>()
    var managerCounts = StatisticsCounts(1, 2, 1, 2)
    val otherCounts = StatisticsCounts(0, 3, 0, 3)
    val service = StatisticsQueryService(members, groups, repository, schedules, refresh)

    init {
        every { members.findByGroupIdAndMemberId(group, any()) } answers { active.firstOrNull { it.memberId == secondArg<ObjectId>() } }
        every { members.findActiveByGroupId(group, null, any()) } answers { active }
        every { members.findByGroupIdIncludingDeleted(group, null, any()) } answers { all }
        every { members.countDistinctActiveMembers(listOf(group)) } answers { mapOf(group to active.size.toLong()) }
        every { groups.loadByGroupId(group.toHexString()) } returns StudyGroup(id = group, name = "고정 회원 통계")
        every { schedules.loadBoundaries(listOf(group), any()) } answers { mapOf(group to boundaries.reversed()) }
        every { refresh.execute(capture(commands)) } answers {
            val command = firstArg<RefreshMemberStatisticsCommand>()
            val membership = all.single { it.identifier == command.membershipId }
            val ledger = ledgers.getOrPut(membership.id!!) { sessionLedger(group, membership.id!!, second.scheduleId) }
            MemberStatisticsDto(group.toHexString(), membership.identifier, membership.memberId.toHexString(),
                command.boundaries.last().session, command.boundaries.last().scheduleAt, ledger.counts, 0.0, 0.0)
        }
        every { repository.findLedgersByGroupId(group) } answers { ledgers.values.toList() }
        every { repository.findGroupSessions(group) } answers { stored.values.toList() }
        every { repository.sumMemberCounts(group, any(), any()) } answers {
            thirdArg<List<MemberSessionStatisticsLedger>>().fold(StatisticsCounts()) { sum, ledger ->
                val counts = if (ledger.membershipId == manager.id) managerCounts else otherCounts
                sum + counts
            }
        }
        every { repository.compareAndSetGroupSession(any(), any()) } answers {
            val next = secondArg<GroupSessionStatistics>()
            if (stored[next.scheduleId]?.version == firstArg<Long?>()) {
                stored[next.scheduleId] = next
                saved += next
                true
            } else false
        }
    }

    fun member(role: GroupMemberRole = GroupMemberRole.MEMBER) = StudyGroupMember(
        id = ObjectId(), groupId = group, memberId = ObjectId(), role = role,
        createdAt = Instant.parse("2026-01-01T00:00:00Z")
    )

    fun execute() = service.execute(GetGroupStatisticsCommand(manager.memberId.toHexString(), group.toHexString()))
}
