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

internal class GroupStatisticsQueryFixture {

    val group = ObjectId()
    val requester = ObjectId()
    val members = mockk<StudyGroupMemberRepository>()
    val groups = mockk<LoadStudyGroupPort>()
    var groupName = "현재 그룹 이름"
    val repository = mockk<StatisticsRepository>()
    val schedules = mockk<ScheduleStatisticsSourcePort>()
    val refresh = mockk<RefreshMemberStatisticsUseCase>()
    var boundaries = listOf(StatisticsSessionBoundary(group, ObjectId(), 3, Instant.parse("2026-09-01T10:00:00Z")),
        StatisticsSessionBoundary(group, ObjectId(), 12, Instant.parse("2026-09-16T10:00:00Z")))
    var ledgers = listOf(sessionLedger(groupId = group, scheduleId = boundaries.last().scheduleId))
    val stored = mutableMapOf<ObjectId, GroupSessionStatistics>()
    var counts = StatisticsCounts(1, 2, 1, 4)
    var activeCount = 3L
    val service = StatisticsQueryService(members, groups, repository, schedules, refresh)

    init {
        every { groups.loadByGroupId(group.toHexString()) } answers { StudyGroup(id = group, name = groupName) }
        val membership = StudyGroupMember(id = ledgers.first().membershipId, groupId = group, memberId = requester,
            role = GroupMemberRole.OWNER, createdAt = Instant.parse("2026-01-01T00:00:00Z"))
        every { members.findByGroupIdAndMemberId(group, requester) } returns membership
        every { members.findByGroupIdIncludingDeleted(group, null, any()) } returns listOf(membership)
        every { members.countDistinctActiveMembers(listOf(group)) } answers { mapOf(group to activeCount) }
        every { schedules.loadBoundaries(listOf(group), any()) } answers { mapOf(group to boundaries) }
        every { refresh.execute(any()) } answers {
            MemberStatisticsDto(group.toHexString(), membership.identifier, requester.toHexString(),
                boundaries.last().session, boundaries.last().scheduleAt, counts, counts.taskSubmissionRate(), counts.scheduleAttendanceRate())
        }
        every { repository.findLedgersByGroupId(group) } answers { ledgers }
        every { repository.findGroupSessions(group) } answers { stored.values.toList() }
        every { repository.findGroupSession(group, any()) } answers { stored[secondArg<ObjectId>()] }
        every { repository.sumMemberCounts(group, any(), any()) } answers { counts }
        every { repository.compareAndSetGroupSession(any(), any()) } answers {
            val snapshot = secondArg<GroupSessionStatistics>()
            stored[snapshot.scheduleId] = snapshot
            true
        }
    }

    fun execute() = service.execute(GetGroupStatisticsCommand(requester.toHexString(), group.toHexString()))
}
